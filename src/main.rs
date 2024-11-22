use config::{Config, VersionType};
use std::fs;
use std::io::{self, BufReader};
mod config;
use std::collections::BTreeSet;
use std::fs::File;
use std::fs::OpenOptions;
use std::io::Write;
use std::path::Path;
use std::process::{Command, Stdio};
use zip::ZipArchive;

fn main() {
    let config: Config = Config::read("config.toml").unwrap();
    update_resources(&config).unwrap();
    find_server_data(&config).unwrap();
}

fn prune_folder(path: &str, whitelist: &Vec<&str>) -> Result<(), Box<dyn std::error::Error>> {
    for entry in fs::read_dir(path)? {
        let path = entry?.path();
        if !whitelist.contains(&path.file_name().and_then(|n| n.to_str()).unwrap_or("")) {
            if path.is_dir() {
                fs::remove_dir_all(&path)?;
            } else {
                fs::remove_file(&path)?;
            }
        }
    }
    Ok(())
}

fn update_resources(config: &Config) -> Result<(), Box<dyn std::error::Error>> {
    println!("Fetching resources (data / assets)");

    if let Err(e) = fs::remove_dir_all("resources") {
        if e.kind() != io::ErrorKind::NotFound {
            return Err(Box::new(e));
        }
    }
    let jar_location: String = format!(
        "{}/versions/{}/{}.jar",
        config.minecraft_install, config.version, config.version
    );
    fs::create_dir("resources")?;
    fs::copy(jar_location, "resources/version.jar")?;

    let jar_file = File::open("resources/version.jar")?;
    let mut archive = ZipArchive::new(BufReader::new(jar_file))?;
    archive.extract("resources")?;
    println!("Extracted Version Jar");

    prune_folder("resources", &vec!["assets", "data"])?;
    println!("Task Finished");

    Ok(())
}

fn find_server_data(config: &Config) -> Result<(), Box<dyn std::error::Error>> {
    println!("Fetching server data");
    let server_download_uri: String = get_server_url(config)?;

    if let Err(e) = fs::remove_file("server.jar") {
        if e.kind() != io::ErrorKind::NotFound {
            return Err(Box::new(e));
        }
    }
    let mut server_jar: File = File::create("resources/server.jar")?;
    let raw_bytes = reqwest::blocking::get(server_download_uri)?.bytes()?;
    server_jar.write_all(&raw_bytes)?;
    println!("Downloaded server jar!");

    run_server_debug_scripts(config)?;
    println!("Ran server debug scripts!");

    if config.version_type == VersionType::Pre114 {
        get_registries_114()?;
    } else {
        get_registries(&[
            "item",
            "block",
//            "enchantment",
            "entity_type",
            "mob_effect",
            "particle_type",
        ])?;
    }
    println!("Parsed server debug data");

    clean_up_server_jar_files()?;

    println!("Finished Task!");

    Ok(())
}

fn get_server_url(config: &Config) -> Result<String, Box<dyn std::error::Error>> {
    let version_json: String = format!(
        "{}/versions/{}/{}.json",
        config.minecraft_install, config.version, config.version
    );
    let data = fs::read_to_string(version_json.as_str())?;
    let json: serde_json::Value = serde_json::from_str(&data)?;
    let url: String = json
        .get("downloads")
        .and_then(|d| d.get("server"))
        .and_then(|s| s.get("url"))
        .and_then(|u| u.as_str())
        .map(String::from)
        .ok_or("URL not found in server JSON")?;
    Ok(url)
}

fn run_server_debug_scripts(config: &Config) -> Result<(), Box<dyn std::error::Error>> {
    let command = if config.version_type == VersionType::Recent {
        "java -DbundlerMainClass=net.minecraft.data.Main -jar server.jar --reports\n"
    } else {
        "java -cp server.jar net.minecraft.data.Main --server --reports"
    };
    let mut process = Command::new("sh")
        .arg("-c")
        .arg(command)
        .current_dir(Path::new("resources"))
        .stdout(Stdio::inherit())
        .stderr(Stdio::inherit())
        .spawn()?;

    let status = process.wait()?;
    if !status.success() {
        return Err(format!("Process failed with exit code: {:?}", status.code()).into());
    }
    Ok(())
}

fn get_registries_114() -> Result<(), Box<dyn std::error::Error>> {
    let reports = "resources/generated/reports";
    let output = "resources/lists/";
    let targets = vec!["blocks", "items"];

    for target in targets {
        let file_path = format!("{}{}.json", reports, target);
        let data = fs::read_to_string(&file_path)?;
        let json: serde_json::Value = serde_json::from_str(&data)?;

        let entries: BTreeSet<_> = json
            .as_object()
            .ok_or("Expected JSON object")?
            .keys()
            .map(|k| k.replace("minecraft:", ""))
            .collect();

        let output_file_path = format!("{}{}.txt", output, target);
        let mut file = OpenOptions::new()
            .create(true)
            .append(true)
            .open(&output_file_path)?;

        for entry in entries {
            writeln!(file, "{}", entry)?;
        }
    }
    Ok(())
}

fn get_registries(keys: &[&str]) -> Result<(), Box<dyn std::error::Error>> {

    let registries = "resources/generated/reports/registries.json";
    let output = "resources/lists/";
    fs::create_dir_all(output)?;
    
    let data = fs::read_to_string(registries)?;
    let json: serde_json::Value = serde_json::from_str(&data)?;
    for key in keys {
        let registry = json
            .get(format!("minecraft:{}", key))
            .ok_or(format!("Key 'minecraft:{}' not found", key))?;

        let entries = registry
            .get("entries")
            .and_then(|e| e.as_object())
            .ok_or("Expected 'entries' to be a JSON object")?;

        let entry_list: BTreeSet<_> = entries
            .keys()
            .map(|k| k.replace("minecraft:", ""))
            .collect();

        let output_file_path = format!("{}{}.txt", output, key);
        let mut file = OpenOptions::new()
            .create(true)
            .append(true)
            .open(&output_file_path)?;

        for entry in entry_list {
            writeln!(file, "{}", entry)?;
        }
    }

    Ok(())
}

fn clean_up_server_jar_files() -> Result<(), Box<dyn std::error::Error>> {
    let paths = [
        "resources/generated",
        "resources/logs",
        "resources/libraries",
        "resources/versions",
        "resources/server.jar",
    ];

    for path in paths.iter() {
        let path = Path::new(path);
        if path.exists() {
            if path.is_dir() {
                fs::remove_dir_all(path)?;
            } else {
                fs::remove_file(path)?;
            }
        }
    }

    Ok(())
}
