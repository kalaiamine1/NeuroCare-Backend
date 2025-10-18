#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
⚙️ Configuration du Microservice IA Rappels
==========================================

Configuration centralisée pour le microservice de génération de rappels.
"""

import os
from typing import Dict, Any

class Config:
    """Configuration de base"""
    
    # Configuration Flask
    SECRET_KEY = os.environ.get('SECRET_KEY', 'ai-reminder-service-secret-key-2024')
    JSON_AS_ASCII = False
    JSONIFY_PRETTYPRINT_REGULAR = True
    
    # Configuration serveur
    HOST = os.environ.get('HOST', '0.0.0.0')
    PORT = int(os.environ.get('PORT', 5000))
    DEBUG = os.environ.get('DEBUG', 'False').lower() == 'true'
    
    # Configuration CORS
    CORS_ORIGINS = [
        'http://localhost:4200',  # Angular dev
        'http://localhost:8089',  # Spring Boot
        'http://127.0.0.1:4200',
        'http://127.0.0.1:8089'
    ]
    
    # Configuration logging
    LOG_LEVEL = os.environ.get('LOG_LEVEL', 'INFO')
    LOG_FILE = os.environ.get('LOG_FILE', 'ai_reminder_service.log')
    
    # Configuration rappels
    MAX_BATCH_SIZE = int(os.environ.get('MAX_BATCH_SIZE', 50))
    DEFAULT_APPOINTMENT_TYPE = 'CONSULTATION'
    
    # Templates de rappels
    REMINDER_TEMPLATES = {
        'CONSULTATION': {
            'title': 'Consultation Médicale',
            'icon': '📋',
            'templates': [
                "Bonjour ! 📋 Rappel important : {child_name} a une consultation avec {professional_name} {time_info}. {location_info} {notes_info}",
                "🔔 N'oubliez pas : Rendez-vous de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                "⏰ Rappel consultation : {child_name} chez {professional_name} {time_info}. {location_info} {notes_info}"
            ]
        },
        'THERAPEUTIC': {
            'title': 'Séance Thérapeutique',
            'icon': '🌟',
            'templates': [
                "🌟 Séance thérapeutique pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                "🧠 Rappel séance : {child_name} a rendez-vous avec {professional_name} {time_info}. {location_info} {notes_info}",
                "💪 N'oubliez pas la séance de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
            ]
        },
        'MEDICAL': {
            'title': 'Rendez-vous Médical',
            'icon': '🏥',
            'templates': [
                "🏥 Rendez-vous médical pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                "⚕️ Rappel médical : {child_name} chez {professional_name} {time_info}. {location_info} {notes_info}",
                "🩺 N'oubliez pas : Consultation médicale de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
            ]
        },
        'EDUCATIONAL': {
            'title': 'Suivi Éducatif',
            'icon': '📚',
            'templates': [
                "📚 Suivi éducatif pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                "🎓 Rappel éducatif : {child_name} a rendez-vous avec {professional_name} {time_info}. {location_info} {notes_info}",
                "✏️ N'oubliez pas le suivi de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
            ]
        }
    }
    
    # Niveaux d'urgence
    URGENCY_LEVELS = {
        'URGENT': {
            'hours': 2,
            'icon': '🚨',
            'color': 'red',
            'priority': 1
        },
        'TODAY': {
            'hours': 24,
            'icon': '📅',
            'color': 'orange',
            'priority': 2
        },
        'TOMORROW': {
            'hours': 48,
            'icon': '⏰',
            'color': 'yellow',
            'priority': 3
        },
        'UPCOMING': {
            'hours': 168,
            'icon': '📋',
            'color': 'blue',
            'priority': 4
        }
    }
    
    # Messages d'erreur
    ERROR_MESSAGES = {
        'INVALID_JSON': 'Content-Type must be application/json',
        'MISSING_FIELDS': 'Champs manquants: {fields}',
        'BATCH_TOO_LARGE': f'Maximum {MAX_BATCH_SIZE} rendez-vous par lot',
        'NO_APPOINTMENTS': 'Aucun rendez-vous fourni',
        'INTERNAL_ERROR': 'Erreur interne du serveur'
    }
    
    # Métadonnées du service
    SERVICE_INFO = {
        'name': 'ai-reminder-service',
        'version': '1.0.0',
        'description': 'Microservice IA pour génération de rappels personnalisés',
        'author': 'Agent IA Expert',
        'endpoints': [
            'GET /health',
            'POST /generate-reminder',
            'POST /batch-reminders',
            'GET /templates'
        ]
    }

class DevelopmentConfig(Config):
    """Configuration pour le développement"""
    DEBUG = True
    LOG_LEVEL = 'DEBUG'

class ProductionConfig(Config):
    """Configuration pour la production"""
    DEBUG = False
    LOG_LEVEL = 'INFO'

class TestingConfig(Config):
    """Configuration pour les tests"""
    TESTING = True
    DEBUG = True
    LOG_LEVEL = 'DEBUG'

# Configuration par défaut selon l'environnement
config_map = {
    'development': DevelopmentConfig,
    'production': ProductionConfig,
    'testing': TestingConfig,
    'default': DevelopmentConfig
}

def get_config(env: str = None) -> Config:
    """
    Retourne la configuration selon l'environnement
    
    Args:
        env: Environnement (development, production, testing)
        
    Returns:
        Configuration appropriée
    """
    if env is None:
        env = os.environ.get('FLASK_ENV', 'default')
    
    config_class = config_map.get(env, config_map['default'])
    return config_class()

