package com.casrusil.siierpai.modules.sso.domain.exception;

import com.casrusil.siierpai.shared.domain.exception.DomainException;

/**
 * La invitación existe pero ya no se puede usar: fue aceptada, revocada o venció.
 * Se responde 410 Gone.
 */
public class InvitationNoLongerValidException extends DomainException {
    public InvitationNoLongerValidException(String message) {
        super(message);
    }
}
