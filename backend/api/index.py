import sys
import os

# Add root backend directory to sys.path so app module is discoverable
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.main import app

