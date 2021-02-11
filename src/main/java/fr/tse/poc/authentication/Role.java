package fr.tse.poc.authentication;

public enum Role {
    Admin {
        public String toString() {
            return "Admin";
        }
    },
    Manager {
        public String toString() {
            return "Manager";
        }
    },
    User {
        public String toString() {
            return "User";
        }
    }
}
