rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // 1. Allow anyone to read vehicle information (so they can select their bike)
    match /makes/{document=**} {
      allow read: if true;
      allow write: if false; // Only your upload.js (Admin) can write here
    }

    // 2. Allow logged-in users to read and update ONLY their own profile/vehicle
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}