package logger

import (
	"log/slog"
	"os"
)

func New(env string) *slog.Logger {
	var level slog.Level
	var addSource bool

	switch env {
	case "dev":
		addSource = true
		level = slog.LevelDebug
	case "prod":
		addSource = false
		level = slog.LevelInfo
	default:
		addSource = false
		level = slog.LevelInfo
	}

	log := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		AddSource: addSource,
		Level:     level,
	}))

	return log
}

func Err(err error) slog.Attr {
	return slog.Attr{
		Key:   "error",
		Value: slog.StringValue(err.Error()),
	}
}
