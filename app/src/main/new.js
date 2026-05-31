rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write without login for development
    match /{document=**} {
      allow read, write: if true;
    }
  }
}