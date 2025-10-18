#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
🤖 Microservice IA pour Génération de Rappels NeuroCare
=======================================================

Ce microservice génère des rappels personnalisés pour les rendez-vous
médicaux et thérapeutiques des enfants avec des besoins spéciaux.

Auteur: Agent IA Expert
Version: 1.0.0
Date: 2024
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime, timedelta
import logging
import os
import json
from typing import Dict, List, Optional, Any
import traceback

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('ai_reminder_service.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# Initialisation de Flask
app = Flask(__name__)
CORS(app, origins=["http://localhost:4200", "http://localhost:8089"])

# Configuration
app.config['JSON_AS_ASCII'] = False
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True

class ReminderGenerator:
    """
    🎯 Générateur de rappels personnalisés
    
    Cette classe génère des messages de rappel adaptés aux différents
    types de rendez-vous et aux besoins spécifiques des enfants.
    """
    
    def __init__(self):
        """Initialise le générateur avec les templates de base"""
        self.templates = self._load_templates()
        self.urgency_levels = {
            'URGENT': {'hours': 2, 'icon': '🚨', 'color': 'red'},
            'TODAY': {'hours': 24, 'icon': '📅', 'color': 'orange'},
            'TOMORROW': {'hours': 48, 'icon': '⏰', 'color': 'yellow'},
            'UPCOMING': {'hours': 168, 'icon': '📋', 'color': 'blue'}
        }
    
    def _load_templates(self) -> Dict[str, Dict[str, str]]:
        """Charge les templates de rappels par type de rendez-vous"""
        return {
            'CONSULTATION': {
                'title': 'Consultation Médicale',
                'templates': [
                    "Bonjour ! 📋 Rappel important : {child_name} a une consultation avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "🔔 N'oubliez pas : Rendez-vous de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "⏰ Rappel consultation : {child_name} chez {professional_name} {time_info}. {location_info} {notes_info}"
                ]
            },
            'THERAPEUTIC': {
                'title': 'Séance Thérapeutique',
                'templates': [
                    "🌟 Séance thérapeutique pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "🧠 Rappel séance : {child_name} a rendez-vous avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "💪 N'oubliez pas la séance de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
                ]
            },
            'MEDICAL': {
                'title': 'Rendez-vous Médical',
                'templates': [
                    "🏥 Rendez-vous médical pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "⚕️ Rappel médical : {child_name} chez {professional_name} {time_info}. {location_info} {notes_info}",
                    "🩺 N'oubliez pas : Consultation médicale de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
                ]
            },
            'EDUCATIONAL': {
                'title': 'Suivi Éducatif',
                'templates': [
                    "📚 Suivi éducatif pour {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "🎓 Rappel éducatif : {child_name} a rendez-vous avec {professional_name} {time_info}. {location_info} {notes_info}",
                    "✏️ N'oubliez pas le suivi de {child_name} avec {professional_name} {time_info}. {location_info} {notes_info}"
                ]
            }
        }
    
    def calculate_urgency(self, appointment_time: str) -> str:
        """
        Calcule le niveau d'urgence basé sur le temps restant
        
        Args:
            appointment_time: Heure du rendez-vous (ISO format)
            
        Returns:
            Niveau d'urgence (URGENT, TODAY, TOMORROW, UPCOMING)
        """
        try:
            appt_dt = datetime.fromisoformat(appointment_time.replace('Z', '+00:00'))
            now = datetime.now(appt_dt.tzinfo) if appt_dt.tzinfo else datetime.now()
            time_diff = appt_dt - now
            hours_remaining = time_diff.total_seconds() / 3600
            
            if hours_remaining <= 2:
                return 'URGENT'
            elif hours_remaining <= 24:
                return 'TODAY'
            elif hours_remaining <= 48:
                return 'TOMORROW'
            else:
                return 'UPCOMING'
                
        except Exception as e:
            logger.warning(f"Erreur calcul urgence: {e}")
            return 'UPCOMING'
    
    def format_time_info(self, appointment_time: str, urgency: str) -> str:
        """
        Formate l'information temporelle selon l'urgence
        
        Args:
            appointment_time: Heure du rendez-vous
            urgency: Niveau d'urgence
            
        Returns:
            Information temporelle formatée
        """
        try:
            appt_dt = datetime.fromisoformat(appointment_time.replace('Z', '+00:00'))
            now = datetime.now(appt_dt.tzinfo) if appt_dt.tzinfo else datetime.now()
            time_diff = appt_dt - now
            
            if urgency == 'URGENT':
                minutes = int(time_diff.total_seconds() / 60)
                if minutes <= 0:
                    return "MAINTENANT !"
                elif minutes < 60:
                    return f"dans {minutes} minutes"
                else:
                    hours = int(minutes / 60)
                    return f"dans {hours}h{minutes % 60:02d}"
            elif urgency == 'TODAY':
                return f"aujourd'hui à {appt_dt.strftime('%H:%M')}"
            elif urgency == 'TOMORROW':
                return f"demain à {appt_dt.strftime('%H:%M')}"
            else:
                return f"le {appt_dt.strftime('%d/%m/%Y à %H:%M')}"
                
        except Exception as e:
            logger.warning(f"Erreur formatage temps: {e}")
            return f"le {appointment_time}"
    
    def generate_reminder(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Génère un rappel personnalisé
        
        Args:
            data: Données du rendez-vous
            
        Returns:
            Rappel généré avec métadonnées
        """
        try:
            # Extraction des données
            child_name = data.get('childName', 'Votre enfant')
            professional_name = data.get('professionalName', 'le professionnel')
            appointment_time = data.get('startTime', '')
            appointment_type = data.get('type', 'CONSULTATION')
            location = data.get('location', '')
            notes = data.get('notes', '')
            
            # Calcul de l'urgence
            urgency = self.calculate_urgency(appointment_time)
            time_info = self.format_time_info(appointment_time, urgency)
            
            # Préparation des informations contextuelles
            location_info = f"📍 {location}" if location else ""
            notes_info = f"📝 {notes}" if notes else ""
            
            # Sélection du template
            template_data = self.templates.get(appointment_type, self.templates['CONSULTATION'])
            templates = template_data['templates']
            
            # Choix du template (rotation simple basée sur l'heure)
            template_index = datetime.now().hour % len(templates)
            template = templates[template_index]
            
            # Génération du message
            message = template.format(
                child_name=child_name,
                professional_name=professional_name,
                time_info=time_info,
                location_info=location_info,
                notes_info=notes_info
            )
            
            # Métadonnées
            urgency_info = self.urgency_levels[urgency]
            
            return {
                'success': True,
                'message': message,
                'urgency': urgency,
                'urgency_icon': urgency_info['icon'],
                'urgency_color': urgency_info['color'],
                'appointment_type': appointment_type,
                'child_name': child_name,
                'professional_name': professional_name,
                'appointment_time': appointment_time,
                'location': location,
                'notes': notes,
                'generated_at': datetime.now().isoformat(),
                'source': 'ai_reminder_service',
                'version': '1.0.0'
            }
            
        except Exception as e:
            logger.error(f"Erreur génération rappel: {e}")
            logger.error(traceback.format_exc())
            return self._generate_fallback_reminder(data)
    
    def _generate_fallback_reminder(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Génère un rappel de secours en cas d'erreur"""
        child_name = data.get('childName', 'Votre enfant')
        professional_name = data.get('professionalName', 'le professionnel')
        appointment_time = data.get('startTime', '')
        appointment_type = data.get('type', 'CONSULTATION')
        
        fallback_message = f"📋 Rappel: {child_name} a un rendez-vous {appointment_type.lower()} avec {professional_name} le {appointment_time}"
        
        return {
            'success': True,
            'message': fallback_message,
            'urgency': 'UPCOMING',
            'urgency_icon': '📋',
            'urgency_color': 'blue',
            'appointment_type': appointment_type,
            'child_name': child_name,
            'professional_name': professional_name,
            'appointment_time': appointment_time,
            'location': data.get('location', ''),
            'notes': data.get('notes', ''),
            'generated_at': datetime.now().isoformat(),
            'source': 'ai_reminder_service_fallback',
            'version': '1.0.0'
        }

# Instance du générateur
reminder_generator = ReminderGenerator()

@app.route('/health', methods=['GET'])
def health_check():
    """
    🏥 Health Check Endpoint
    
    Vérifie l'état du microservice
    """
    try:
        return jsonify({
            'status': 'healthy',
            'service': 'ai-reminder-service',
            'version': '1.0.0',
            'timestamp': datetime.now().isoformat(),
            'uptime': 'running',
            'endpoints': [
                '/health',
                '/generate-reminder',
                '/batch-reminders'
            ]
        }), 200
    except Exception as e:
        logger.error(f"Erreur health check: {e}")
        return jsonify({
            'status': 'unhealthy',
            'error': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500

@app.route('/generate-reminder', methods=['POST'])
def generate_single_reminder():
    """
    📨 Génération d'un rappel unique
    
    Génère un rappel personnalisé pour un rendez-vous
    """
    try:
        # Validation des données
        if not request.is_json:
            return jsonify({
                'success': False,
                'error': 'Content-Type must be application/json'
            }), 400
        
        data = request.get_json()
        
        # Validation des champs requis
        required_fields = ['childName', 'startTime']
        missing_fields = [field for field in required_fields if not data.get(field)]
        
        if missing_fields:
            return jsonify({
                'success': False,
                'error': f'Champs manquants: {", ".join(missing_fields)}'
            }), 400
        
        logger.info(f"Génération rappel pour {data.get('childName')} - {data.get('type', 'CONSULTATION')}")
        
        # Génération du rappel
        result = reminder_generator.generate_reminder(data)
        
        logger.info(f"Rappel généré avec succès - Urgence: {result['urgency']}")
        
        return jsonify(result), 200
        
    except Exception as e:
        logger.error(f"Erreur génération rappel unique: {e}")
        logger.error(traceback.format_exc())
        return jsonify({
            'success': False,
            'error': 'Erreur interne du serveur',
            'details': str(e)
        }), 500

@app.route('/batch-reminders', methods=['POST'])
def generate_batch_reminders():
    """
    📦 Génération de rappels en lot
    
    Génère plusieurs rappels en une seule requête
    """
    try:
        # Validation des données
        if not request.is_json:
            return jsonify({
                'success': False,
                'error': 'Content-Type must be application/json'
            }), 400
        
        data = request.get_json()
        appointments = data.get('appointments', [])
        
        if not appointments:
            return jsonify({
                'success': False,
                'error': 'Aucun rendez-vous fourni'
            }), 400
        
        if len(appointments) > 50:  # Limite de sécurité
            return jsonify({
                'success': False,
                'error': 'Maximum 50 rendez-vous par lot'
            }), 400
        
        logger.info(f"Génération batch pour {len(appointments)} rendez-vous")
        
        # Génération des rappels
        reminders = []
        errors = []
        
        for i, appointment in enumerate(appointments):
            try:
                reminder = reminder_generator.generate_reminder(appointment)
                reminders.append(reminder)
            except Exception as e:
                logger.warning(f"Erreur rappel {i}: {e}")
                errors.append({
                    'index': i,
                    'error': str(e),
                    'appointment': appointment
                })
        
        result = {
            'success': True,
            'count': len(reminders),
            'total_requested': len(appointments),
            'errors_count': len(errors),
            'reminders': reminders,
            'errors': errors,
            'generated_at': datetime.now().isoformat(),
            'source': 'ai_reminder_service_batch'
        }
        
        logger.info(f"Batch terminé: {len(reminders)} rappels générés, {len(errors)} erreurs")
        
        return jsonify(result), 200
        
    except Exception as e:
        logger.error(f"Erreur génération batch: {e}")
        logger.error(traceback.format_exc())
        return jsonify({
            'success': False,
            'error': 'Erreur interne du serveur',
            'details': str(e)
        }), 500

@app.route('/templates', methods=['GET'])
def get_templates():
    """
    📋 Récupération des templates disponibles
    
    Retourne la liste des templates de rappels
    """
    try:
        return jsonify({
            'success': True,
            'templates': reminder_generator.templates,
            'urgency_levels': reminder_generator.urgency_levels,
            'generated_at': datetime.now().isoformat()
        }), 200
    except Exception as e:
        logger.error(f"Erreur récupération templates: {e}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.errorhandler(404)
def not_found(error):
    """Gestionnaire d'erreur 404"""
    return jsonify({
        'success': False,
        'error': 'Endpoint non trouvé',
        'available_endpoints': [
            '/health',
            '/generate-reminder',
            '/batch-reminders',
            '/templates'
        ]
    }), 404

@app.errorhandler(500)
def internal_error(error):
    """Gestionnaire d'erreur 500"""
    logger.error(f"Erreur interne: {error}")
    return jsonify({
        'success': False,
        'error': 'Erreur interne du serveur'
    }), 500

if __name__ == '__main__':
    # Configuration du serveur
    port = int(os.environ.get('PORT', 5000))
    debug = os.environ.get('DEBUG', 'False').lower() == 'true'
    
    logger.info(f"🚀 Démarrage du microservice IA Rappels sur le port {port}")
    logger.info(f"📋 Endpoints disponibles:")
    logger.info(f"   - GET  /health")
    logger.info(f"   - POST /generate-reminder")
    logger.info(f"   - POST /batch-reminders")
    logger.info(f"   - GET  /templates")
    
    app.run(
        host='0.0.0.0',
        port=port,
        debug=debug,
        threaded=True
    )

