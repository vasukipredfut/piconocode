import frappe
import random
#from frappe.auth import get_password_hash

@frappe.whitelist(allow_guest=True)
def register_fuel_attendant(full_name, phone_number, email, station_address):
    """Register fuel attendar and generate OTP"""
    if frappe.db.exists("Fuel Attendar", {"phone_number": phone_number}):
        return {"status": "error", "message": "Phone number already exists"}

    otp = random.randint(100000, 999999)
    
    # Store user details with OTP
    doc = frappe.get_doc({
        "doctype": "Fuel Attendar",
        "full_name": full_name,
        "phone_number": phone_number,
        "email": email,
        "station_address": station_address,
        "otp": otp,
	#"password":password,
	#"previous_price": previous_price,
	#"brand_name": brand_name,
        #"station_code": station_code,
        "status": "Pending"
    })
    doc.insert(ignore_permissions=True)
    
    # Call SMS API to send OTP
    send_otp_sms(phone_number, otp)
    
    return {"status": "success", "message": "OTP sent", "otp": otp}

def send_otp_sms(phone_number, otp):
    """Integrate with an SMS Gateway (Twilio, MSG91, Fast2SMS, etc.)"""
    import requests
    
    url = "https://www.fast2sms.com/dev/bulkV2"
    payload = {
        "variables_values": otp,
        "route": "otp",
        "numbers": phone_number
    }
    headers = {
        "authorization": "aJ7zZQ5rBF1PDE4fCohG2S68sij3lReMnINgvHOcYWmxKkLuqtMcE4Twqrm05WseSZJkvYQatpLbgoxC",
        "Content-Type": "application/x-www-form-urlencoded"
    }
    
    try:
        response = requests.post(url, data=payload, headers=headers)
        print("SMS API Response:", response.status_code, response.text)  # Debugging
    except Exception as e:
        print("Error Sending SMS:", str(e))  # Debugging


@frappe.whitelist(allow_guest=True)
def verify_otp(mobile, otp, password):
    """Verify OTP and set password"""
    user = frappe.get_value("Fuel Attendar", {"phone_number": mobile}, ["name", "otp", "status"], as_dict=True)
    
    if not user:
        return {"status": "error", "message": "User not found"}
    
    if user["status"] == "Verified":
        return {"status": "error", "message": "User already verified"}
    
    if str(user["otp"]) != str(otp):
        return {"status": "error", "message": "Invalid OTP"}
    
    # Update record and store password securely
    hashed_password = get_password_hash(password)
    frappe.db.set_value("Fuel Attendar", user["name"], {
        "status": "Verified",
        "password": hashed_password
    })
    
    return {"status": "success", "message": "OTP verified, account created"}

