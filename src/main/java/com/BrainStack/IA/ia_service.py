# ia_service_ml.py
from flask import Flask, request, jsonify
import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# Encodage difficulté
difficulty_map = {"facile": 0, "moyen": 1, "difficile": 2}

def age_range_to_mean(ageRange):
    try:
        parts = ageRange.replace(" ans","").split("-")
        return (int(parts[0]) + int(parts[1])) / 2
    except:
        return 0

@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.get_json()
    child = data.get('child', {})
    activities = data.get('activities', [])

    # Profil enfant
    child_age = child.get("age", 0)
    child_prefs = [p.lower() for p in child.get("preferences", [])]

    # Liste des catégories pour vectorisation
    all_categories = [a.get("category","").lower() for a in activities]
    vectorizer = CountVectorizer()
    category_matrix = vectorizer.fit_transform(all_categories).toarray()

    # Construire une matrice d'activités
    activity_features = []
    for i, a in enumerate(activities):
        age_mean = age_range_to_mean(a.get("ageRange","0-100"))
        diff = difficulty_map.get(a.get("difficulty","facile").lower(), 0)
        cat_vec = category_matrix[i]
        feat = [age_mean, diff] + cat_vec.tolist()
        activity_features.append(feat)

    activity_features = np.array(activity_features)

    # Vecteur enfant
    child_cat_vec = np.zeros(category_matrix.shape[1])
    for i, cat in enumerate(vectorizer.get_feature_names_out()):
        if cat in child_prefs:
            child_cat_vec[i] = 1
    child_vec = np.array([child_age, 1] + child_cat_vec.tolist())  # difficulté moyenne=1

    # Cosine similarity
    scores = cosine_similarity(activity_features, child_vec.reshape(1, -1)).flatten()

    # Retourner top 10
    top_indices = np.argsort(scores)[::-1][:10]
    results = [{"activityId": activities[i]["id"], "score": float(scores[i])} for i in top_indices]

    return jsonify(results), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
