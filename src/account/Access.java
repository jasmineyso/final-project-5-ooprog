package src.account;

/** Authorization checks for the project's Admin and RegularUser accounts. */
public final class Access {
    private Access() {
        // Utility class.
    }

    /** Throws when the actor is not an authenticated Admin account. */
    public static void requireAdmin(User actor) {
        if (!(actor instanceof Admin)) {
            throw new SecurityException("Admin access required.");
        }
    }

    /**
     * Allows an Admin or the account whose username owns the record.
     * Usernames are compared without regard to capitalization, matching login behavior.
     */
    public static void requireOwnerOrAdmin(User actor, String ownerUsername) {
        if (actor == null) {
            throw new SecurityException("An authenticated account is required.");
        }
        if (actor instanceof Admin) {
            return;
        }
        String actorUsername = actor.getUsername();
        if (actorUsername == null || ownerUsername == null
                || !actorUsername.equalsIgnoreCase(ownerUsername)) {
            throw new SecurityException("You can only modify your own records.");
        }
    }
}
