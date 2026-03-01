package config

import (
	"fmt"
	"os"
	"time"

	"github.com/ilyakaznacheev/cleanenv"
)

const defaultConfigPath = ".env"

type Config struct {
	Env      string `env:"ENV"`
	Server   Server
	Postgres Postgres
	JWT      JWT
}

type Server struct {
	Host            string        `env:"HOST"`
	Port            int           `env:"APP_PORT"`
	ReadTimeout     time.Duration `env:"READ_TIMEOUT"`
	WriteTimeout    time.Duration `env:"WRITE_TIMEOUT"`
	IdleTimeout     time.Duration `env:"IDLE_TIMEOUT"`
	ShutdownTimeout time.Duration `env:"SHUTDOWN_TIMEOUT"`
}

type Postgres struct {
	DbHost     string `env:"DB_HOST"`
	DbPort     int    `env:"DB_PORT"`
	DbUser     string `env:"DB_USER"`
	DbPassword string `env:"DB_PASSWORD"`
	DbName     string `env:"DB_NAME"`
	DbSslMode  string `env:"DB_SSL_MODE"`
}

type JWT struct {
	Secret          string        `env:"JWT_SECRET"`
	AccessDuration  time.Duration `env:"JWT_ACCESS_DURATION"`
	RefreshDuration time.Duration `env:"JWT_REFRESH_DURATION"`
}

func Parse() (*Config, error) {
	var cfg Config

	configPath := os.Getenv("CONFIG")
	if configPath == "" {
		configPath = defaultConfigPath
	}

	if err := cleanenv.ReadConfig(configPath, &cfg); err != nil {
		fmt.Printf("failed to read config file, reading from env vars '%s': %s\n", configPath, err)
		if err := cleanenv.ReadEnv(&cfg); err != nil {
			return nil, fmt.Errorf("failed to read env vars: %w", err)
		}
	}

	return &cfg, nil
}
