from flask import Flask, request, jsonify
import moviepy.editor as mp
from moviepy.editor import VideoFileClip
import tempfile
from transformers import pipeline
import os
import whisper

# ======= Configuration FFmpeg pour Windows =======
ffmpeg_path = "C:\\Users\\azizc\\Downloads\\ffmpeg-8.0-essentials_build\\ffmpeg-8.0-essentials_build\\bin\\ffmpeg.exe"
os.environ["PATH"] += os.pathsep + ffmpeg_path
# MoviePy utilise cette configuration
import moviepy.config as mp_conf
mp_conf.change_settings({"FFMPEG_BINARY": ffmpeg_path})

# Whisper utilise la variable d'environnement
os.environ["FFMPEG_BINARY"] = ffmpeg_path

# ======= Configuration Flask =======
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # 500 Mo max pour les vidéos

print("MoviePy fonctionne !", mp.__file__)

# ======= Initialisation des modèles =======
# Modèle de résumé léger pour éviter problèmes mémoire
summarizer = pipeline(
    "summarization",
    model="sshleifer/distilbart-cnn-12-6",  # modèle plus léger
    device=-1  # CPU uniquement
)

# Modèle speech-to-text Whisper
speech_model = whisper.load_model("base")  # ou tiny/medium selon ressources

# ======= Endpoint pour résumer vidéo =======
@app.route("/summarize", methods=["POST"])
def summarize_video():
    if "video" not in request.files:
        return jsonify({"error": "No video uploaded"}), 400

    video_file = request.files["video"]

    # Créer fichier temporaire pour la vidéo
    with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as temp_video:
        video_path = temp_video.name
        video_file.save(video_path)

    audio_path = None
    try:
        # 1️⃣ Extraire audio
        clip = VideoFileClip(video_path)
        audio_path = video_path.replace(".mp4", ".wav")
        clip.audio.write_audiofile(audio_path, fps=16000, verbose=False, logger=None)
        clip.close()  # ⚡ Important : libérer le fichier vidéo

        # 2️⃣ Transcrire audio avec Whisper
        result = speech_model.transcribe(audio_path)
        transcript = result["text"]

        # 3️⃣ Résumer texte
        max_chunk = 500  # découpage en segments
        transcript_chunks = [transcript[i:i+max_chunk] for i in range(0, len(transcript), max_chunk)]
        summaries = [
            summarizer(chunk, max_length=100, min_length=30, do_sample=False)[0]['summary_text']
            for chunk in transcript_chunks
        ]
        final_summary = " ".join(summaries)

    finally:
        # Nettoyer fichiers temporaires
        if os.path.exists(video_path):
            os.remove(video_path)
        if audio_path and os.path.exists(audio_path):
            os.remove(audio_path)

    return jsonify({"summary": final_summary}), 200

# ======= Lancer le service =======
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
