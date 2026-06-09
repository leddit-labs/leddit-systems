package dk.ek.gameapi.rest.dto;

import dk.ek.gameapi.entity.Designer;

import java.time.LocalDate;

public record DesignerResponse(
        Integer id,
        String name,
        LocalDate dob
) {
    public static DesignerResponse from(Designer designer) {
        return new DesignerResponse(
                designer.getId(),
                designer.getName(),
                designer.getDob()
        );
    }
}
