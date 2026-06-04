import uuid

# In-memory storage
users_db = {}          # phone -> user_data
session_logs_db = {}   # user_id -> [session_logs]
therapy_progress_db = {} # user_id -> [progress_records]
appointments_db = {}   # user_id -> [appointments]
chat_history_db = {}   # user_id -> {messages: [...]}

# User operations
def add_user(user_data):
    user_id = str(uuid.uuid4())
    user_data['_id'] = user_id
    users_db[user_data['phone']] = user_data
    return user_id

def get_user(user_id):
    for user in users_db.values():
        if user['_id'] == user_id:
            return user
    return None

def update_user(user_id, update_data):
    for user in users_db.values():
        if user['_id'] == user_id:
            user.update(update_data)
            return

def get_userid_by_phone(phone):
    user = users_db.get(phone)
    if user:
        return user['_id']
    # Auto-create user if not found
    new_id = add_user({'phone': phone, 'has_interacted_before': False})
    return new_id

def verify_user(phone):
    return phone in users_db

def has_interacted_before(phone):
    user = users_db.get(phone)
    if user is not None:
        return user.get('has_interacted_before', False)
    return False

def set_interacted_before(phone):
    if phone in users_db:
        users_db[phone]['has_interacted_before'] = True
    return True


# Session logs operations
def add_session_log(session_data):
    user_id = session_data.get('user_id')
    if user_id not in session_logs_db:
        session_logs_db[user_id] = []
    session_logs_db[user_id].append(session_data)
    return len(session_logs_db[user_id]) - 1

def get_session_logs(user_id):
    return session_logs_db.get(user_id, [])

# Therapy progress operations
def add_therapy_progress(progress_data):
    user_id = progress_data.get('user_id')
    if user_id not in therapy_progress_db:
        therapy_progress_db[user_id] = []
    therapy_progress_db[user_id].append(progress_data)
    return len(therapy_progress_db[user_id]) - 1

def get_therapy_progress(user_id):
    return therapy_progress_db.get(user_id, [])

def update_therapy_progress(progress_id, update_data):
    for records in therapy_progress_db.values():
        if progress_id < len(records):
            records[progress_id].update(update_data)
            return

# Appointment operations
def book_appointment(userid, appointment_data):
    if userid not in appointments_db:
        appointments_db[userid] = []
    appointments_db[userid].append(appointment_data)
    return len(appointments_db[userid]) - 1

def get_appointments(user_id):
    return appointments_db.get(user_id, [])

def update_appointment(appointment_id, update_data):
    for records in appointments_db.values():
        if appointment_id < len(records):
            records[appointment_id].update(update_data)
            return

def delete_appointment(appointment_id):
    for user_id, records in appointments_db.items():
        if appointment_id < len(records):
            records.pop(appointment_id)
            return

# Chat history operations
def set_chat_history(user_id, message_data):
    """
    Creates a new chat history record or updates an existing one for a user.
    """
    if user_id not in chat_history_db:
        chat_history_db[user_id] = {'messages': []}
    chat_history_db[user_id]['messages'].extend(message_data)

def get_chat_history(user_id):
    """
    Retrieves the chat history for a specific user.
    """
    return chat_history_db.get(user_id)

def update_chat_history(user_id, update_data):
    """
    Updates the chat history record for a specific user.
    """
    if user_id in chat_history_db:
        chat_history_db[user_id].update(update_data)