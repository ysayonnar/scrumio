package app

import (
	"auth-service/internal/config"
	"auth-service/internal/db"
	"auth-service/internal/handler"
	"auth-service/internal/logger"
	"fmt"
	"log/slog"
	"net/http"
)

func Run() {
	cfg, err := config.Parse()
	if err != nil {
		panic(fmt.Errorf("error parsing config: %w", err))
	}

	log := logger.New(cfg.Env)

	pqConn, err := db.NewPostgres(cfg)
	if err != nil {
		panic(fmt.Errorf("error connect to postgres: %w", err))
	}

	log.Info("Connected to Postgres")

	rdb, err := db.NewRedis(cfg)
	if err != nil {
		panic(fmt.Errorf("error connect to redis: %w", err))
	}

	log.Info("Connected to Redis")

	mux := http.NewServeMux()
	authRouter := handler.NewAuthRouter(cfg, log, pqConn, rdb)
	authRouter.RegisterRoutes(mux)

	server := http.Server{
		Addr:         fmt.Sprintf("%s:%d", cfg.Server.Host, cfg.Server.Port),
		Handler:      mux,
		ReadTimeout:  cfg.Server.ReadTimeout,
		WriteTimeout: cfg.Server.WriteTimeout,
		IdleTimeout:  cfg.Server.IdleTimeout,
	}

	log.Info("Server started", slog.String("host", cfg.Server.Host), slog.Int("port", cfg.Server.Port))
	if err := server.ListenAndServe(); err != nil {
		panic(err)
	}
}
