package fr.tse.poc.exceptions;

import fr.tse.poc.authentication.AuthenticableUserDetails;

public class UnauthorizedException extends RuntimeException {
    public final AuthenticableUserDetails authenticableUserDetails;

    public UnauthorizedException(String message, AuthenticableUserDetails authenticableUserDetails) {
        super(message);
        this.authenticableUserDetails = authenticableUserDetails;
    }
}
