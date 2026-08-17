package com.example.data.firebase

/**
 * Firebase Firestore & Cloud Architecture Schema definitions.
 * Prepares "Arı Takip" for seamless cloud sync, security rules enforcement, and user separation.
 */

data class FirestoreUser(
  val uid: String = "",
  val email: String = "",
  val displayName: String = "",
  val createdAt: Long = 0L
)

data class FirestoreApiary(
  val id: String = "",
  val userId: String = "",
  val name: String = "",
  val address: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val notes: String = "",
  val createdAt: Long = 0L,
  val updatedAt: Long = 0L,
  val isActive: Boolean = true
)

data class FirestoreHive(
  val id: String = "",
  val userId: String = "",
  val apiaryId: String = "",
  val hiveNumber: Int = 0,
  val photoUrl: String? = null,
  val hiveType: String = "",
  val queenYear: Int = 0,
  val queenBreed: String = "",
  val colonyStrength: String = "",
  val notes: String = "",
  val status: String = "active",
  val createdAt: Long = 0L,
  val updatedAt: Long = 0L
)

data class FirestoreInspection(
  val id: String = "",
  val userId: String = "",
  val hiveId: String = "",
  val apiaryId: String = "",
  val inspectionDate: Long = 0L,
  val queenSeen: Boolean = false,
  val broodEgg: Boolean = false,
  val broodLarva: Boolean = false,
  val broodCapped: Boolean = false,
  val colonyStrength: String = "",
  val honeyStatus: String = "",
  val pollenStatus: String = "",
  val behavior: String = "",
  val frameChanges: String = "",
  val problems: String = "",
  val notes: String = "",
  val weatherSummary: String = "",
  val photoUrls: List<String> = emptyList(),
  val createdAt: Long = 0L
)

data class FirestoreFeeding(
  val id: String = "",
  val userId: String = "",
  val hiveId: String = "",
  val apiaryId: String = "",
  val feedingType: String = "",
  val amount: Double = 0.0,
  val unit: String = "",
  val feedingDate: Long = 0L,
  val notes: String = "",
  val photoUrl: String? = null,
  val createdAt: Long = 0L
)

object FirebaseSecurityRules {
  const val FIRESTORE_RULES = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper function to check authenticated user
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Users Collection
    match /users/{userId} {
      allow read, write: if isOwner(userId);
    }

    // Apiaries Collection
    match /apiaries/{apiaryId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }

    // Hives Collection
    match /hives/{hiveId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }

    // Inspections Collection
    match /hiveInspections/{inspectionId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }

    // Feedings Collection
    match /feedings/{feedingId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }

    // Photos Collection
    match /photos/{photoId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }

    // Reminders Collection
    match /reminders/{reminderId} {
      allow read, update, delete: if isOwner(resource.data.userId);
      allow create: if isOwner(request.resource.data.userId);
    }
  }
}
"""

  const val STORAGE_RULES = """
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
"""
}
