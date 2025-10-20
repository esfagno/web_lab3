package ru.example.lab3.validation;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import ru.example.lab3.util.Constants;

@FacesValidator("inputValidator")
@RequestScoped
public class InputValidator implements Validator<Double> {

    @Override
    public void validate(FacesContext context, UIComponent component, Double value) throws ValidatorException {
        if (value == null) {
            return;
        }

        String clientId = component.getClientId(context);

        if (clientId.endsWith("yValue")) {
            if (value <= Constants.Y_MIN || value >= Constants.Y_MAX) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Y out of range",
                        "Y must be in range (" + Constants.Y_MIN + ", " + Constants.Y_MAX + ")"
                ));
            }
        } else if (clientId.endsWith("rValue")) {
            if (value <= Constants.R_MIN || value >= Constants.R_MAX) {
                throw new ValidatorException(new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "R out of range",
                        "R must be in range (" + Constants.R_MIN + ", " + Constants.R_MAX + ")"
                ));
            }
        }
    }
}