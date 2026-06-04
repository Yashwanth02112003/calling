import os
from dotenv import load_dotenv

load_dotenv()

TWILIO_ACCOUNT_SID = os.getenv("TWILIO_ACCOUNT_SID", "")
TWILIO_AUTH_TOKEN = os.getenv("TWILIO_AUTH_TOKEN", "")
TWILIO_PHONE_NUMBER = os.getenv("TWILIO_PHONE_NUMBER", "")
PERSONAL_PHONE = os.getenv("PERSONAL_PHONE", "")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
SERVER_URL = os.getenv("SERVER_URL", "")


FLASK_APP = 'app.py'
FLASK_ENV = 'development'
