use serde::{Deserialize, Serialize};
use std::fs;
#[derive(Debug, Deserialize, PartialEq, Serialize)]
pub enum VersionType {
    #[serde(rename = "pre 1.14")]
    Pre114,
    #[serde(rename = "pre 1.18")]
    Pre118,
    #[serde(rename = "recent")]
    Recent,
}

#[derive(Debug, Deserialize)]
pub struct Config {
    pub version: String,
    pub version_type: VersionType,
    pub minecraft_install: String,
}

impl Config {
    pub fn read(path: &str) -> Result<Config, Box<dyn std::error::Error>> {
        let content = fs::read_to_string(path)?;
        let config: Config = toml::from_str(&content)?;
        Ok(config)
    }
}
