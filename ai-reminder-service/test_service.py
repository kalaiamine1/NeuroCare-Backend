#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
🧪 Tests pour le Microservice IA Rappels
=======================================

Script de test pour valider le fonctionnement du microservice.
"""

import requests
import json
import time
from datetime import datetime, timedelta

# Configuration
BASE_URL = "http://localhost:5000"
TEST_DATA = {
    "childName": "Alice",
    "professionalName": "Dr. Martin",
    "startTime": (datetime.now() + timedelta(hours=2)).isoformat(),
    "type": "CONSULTATION",
    "location": "Cabinet médical",
    "notes": "Apporter le carnet de santé"
}

def test_health_check():
    """Test du health check"""
    print("🔍 Test Health Check...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Health Check OK - Status: {data['status']}")
            return True
        else:
            print(f"❌ Health Check FAILED - Status: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Health Check ERROR: {e}")
        return False

def test_single_reminder():
    """Test de génération d'un rappel unique"""
    print("🔍 Test Rappel Unique...")
    try:
        response = requests.post(
            f"{BASE_URL}/generate-reminder",
            json=TEST_DATA,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Rappel généré - Urgence: {data['urgency']}")
            print(f"📝 Message: {data['message']}")
            return True
        else:
            print(f"❌ Rappel FAILED - Status: {response.status_code}")
            print(f"📝 Response: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Rappel ERROR: {e}")
        return False

def test_batch_reminders():
    """Test de génération de rappels en lot"""
    print("🔍 Test Rappels en Lot...")
    
    batch_data = {
        "appointments": [
            {
                "childName": "Alice",
                "professionalName": "Dr. Martin",
                "startTime": (datetime.now() + timedelta(hours=1)).isoformat(),
                "type": "CONSULTATION"
            },
            {
                "childName": "Bob",
                "professionalName": "Mme. Dupont",
                "startTime": (datetime.now() + timedelta(days=1)).isoformat(),
                "type": "THERAPEUTIC",
                "location": "Centre thérapeutique"
            },
            {
                "childName": "Charlie",
                "professionalName": "Dr. Smith",
                "startTime": (datetime.now() + timedelta(days=2)).isoformat(),
                "type": "MEDICAL",
                "location": "Hôpital",
                "notes": "Examen spécialisé"
            }
        ]
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/batch-reminders",
            json=batch_data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Batch généré - {data['count']} rappels")
            for i, reminder in enumerate(data['reminders']):
                print(f"   {i+1}. {reminder['child_name']} - {reminder['urgency']}")
            return True
        else:
            print(f"❌ Batch FAILED - Status: {response.status_code}")
            print(f"📝 Response: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Batch ERROR: {e}")
        return False

def test_templates():
    """Test de récupération des templates"""
    print("🔍 Test Templates...")
    try:
        response = requests.get(f"{BASE_URL}/templates")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Templates récupérés - {len(data['templates'])} types")
            for template_type, template_data in data['templates'].items():
                print(f"   - {template_type}: {template_data['title']}")
            return True
        else:
            print(f"❌ Templates FAILED - Status: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Templates ERROR: {e}")
        return False

def test_error_handling():
    """Test de gestion d'erreurs"""
    print("🔍 Test Gestion d'Erreurs...")
    
    # Test avec données manquantes
    try:
        response = requests.post(
            f"{BASE_URL}/generate-reminder",
            json={"childName": "Test"},
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 400:
            print("✅ Validation des données OK")
        else:
            print(f"❌ Validation FAILED - Status: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Validation ERROR: {e}")
        return False
    
    # Test avec endpoint inexistant
    try:
        response = requests.get(f"{BASE_URL}/nonexistent")
        if response.status_code == 404:
            print("✅ Gestion 404 OK")
        else:
            print(f"❌ Gestion 404 FAILED - Status: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Gestion 404 ERROR: {e}")
        return False
    
    return True

def test_urgency_levels():
    """Test des différents niveaux d'urgence"""
    print("🔍 Test Niveaux d'Urgence...")
    
    urgency_tests = [
        {
            "name": "URGENT",
            "data": {**TEST_DATA, "startTime": (datetime.now() + timedelta(minutes=30)).isoformat()}
        },
        {
            "name": "TODAY",
            "data": {**TEST_DATA, "startTime": (datetime.now() + timedelta(hours=2)).isoformat()}
        },
        {
            "name": "TOMORROW",
            "data": {**TEST_DATA, "startTime": (datetime.now() + timedelta(days=1)).isoformat()}
        },
        {
            "name": "UPCOMING",
            "data": {**TEST_DATA, "startTime": (datetime.now() + timedelta(days=3)).isoformat()}
        }
    ]
    
    for test in urgency_tests:
        try:
            response = requests.post(
                f"{BASE_URL}/generate-reminder",
                json=test["data"],
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if data['urgency'] == test['name']:
                    print(f"✅ {test['name']} - {data['urgency_icon']}")
                else:
                    print(f"❌ {test['name']} - Attendu: {test['name']}, Reçu: {data['urgency']}")
                    return False
            else:
                print(f"❌ {test['name']} FAILED - Status: {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ {test['name']} ERROR: {e}")
            return False
    
    return True

def main():
    """Fonction principale de test"""
    print("🧪 Tests du Microservice IA Rappels")
    print("===================================")
    print()
    
    # Vérification que le service est démarré
    print("⏳ Vérification du service...")
    max_retries = 10
    for i in range(max_retries):
        try:
            response = requests.get(f"{BASE_URL}/health", timeout=2)
            if response.status_code == 200:
                print("✅ Service disponible")
                break
        except:
            if i < max_retries - 1:
                print(f"⏳ Tentative {i+1}/{max_retries}...")
                time.sleep(2)
            else:
                print("❌ Service non disponible après 10 tentatives")
                print("💡 Assurez-vous que le service est démarré avec: python3 app.py")
                return False
    
    print()
    
    # Exécution des tests
    tests = [
        ("Health Check", test_health_check),
        ("Rappel Unique", test_single_reminder),
        ("Rappels en Lot", test_batch_reminders),
        ("Templates", test_templates),
        ("Gestion d'Erreurs", test_error_handling),
        ("Niveaux d'Urgence", test_urgency_levels)
    ]
    
    results = []
    for test_name, test_func in tests:
        print(f"🔍 {test_name}...")
        result = test_func()
        results.append((test_name, result))
        print()
    
    # Résumé des résultats
    print("📊 Résumé des Tests")
    print("==================")
    passed = 0
    for test_name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status} {test_name}")
        if result:
            passed += 1
    
    print()
    print(f"📈 Résultat: {passed}/{len(results)} tests réussis")
    
    if passed == len(results):
        print("🎉 Tous les tests sont passés ! Le service fonctionne correctement.")
    else:
        print("⚠️  Certains tests ont échoué. Vérifiez les logs du service.")
    
    return passed == len(results)

if __name__ == "__main__":
    main()

