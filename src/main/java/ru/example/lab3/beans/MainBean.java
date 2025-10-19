package ru.example.lab3.beans;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import ru.example.lab3.model.HitResult;
import ru.example.lab3.service.HitCheckService;
import ru.example.lab3.validation.ValidationException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Named("mainBean")
@ViewScoped
public class MainBean implements Serializable {

    @Getter @Setter private Double x;
    @Getter @Setter private Double y;
    @Getter @Setter private Double r = 1.0;

    @Getter private boolean submitted = false;
    @Getter private boolean lastHit = false;

    private static final List<Double> X_VALUES = Arrays.asList(-4.0, -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0);
    public List<Double> getXValues() { return X_VALUES; }

    @Inject private HitCheckService service;
    @Inject private ResultsManager resultsManager;

    private static final Logger logger = Logger.getLogger(MainBean.class.getName());


    public void checkPoint() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        String xParam = request.getParameter("canvasX");
        String yParam = request.getParameter("canvasY");

        if (xParam != null && yParam != null) {
            try {
                this.x = Double.parseDouble(xParam);
                this.y = Double.parseDouble(yParam);
                logger.info(() -> "Canvas input: x=" + x + ", y=" + y);
            } catch (NumberFormatException e) {
                logger.warning("Invalid canvas parameters: " + xParam + ", " + yParam);
                context.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Error", "Invalid coordinates from canvas"
                ));
                return;
            }
        }

        if (r == null) {
            String rParam = request.getParameter("canvasR");
            if (rParam != null) {
                try {
                    this.r = Double.parseDouble(rParam);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (x == null || y == null || r == null) {
            logger.warning("One or more fields are null");
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Validation Error",
                    "Please fill all required fields (X, Y, R)."
            ));
            submitted = false;
            return;
        }

        try {
            HitResult result = service.checkHit(x, y, r);
            lastHit = result.isHit();
            submitted = true;
            resultsManager.addResult(result);
            logger.info("Result added: " + result);
        } catch (ValidationException e) {
            logger.warning("Validation failed: " + e.getMessage());
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Validation failed", e.getMessage()
            ));
            submitted = false;
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error", "Invalid input"
            ));
            submitted = false;
        }
    }
}