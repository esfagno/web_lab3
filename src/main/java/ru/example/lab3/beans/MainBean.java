package ru.example.lab3.beans;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import ru.example.lab3.model.HitResult;
import ru.example.lab3.service.HitCheckService;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

@Named("mainBean")
@ViewScoped
public class MainBean implements Serializable {

    private static final Logger logger = Logger.getLogger(MainBean.class.getName());
    @Getter
    @Setter
    private Double x;
    @Getter
    @Setter
    private Double y;
    @Getter
    @Setter
    private Double r = 3.0;
    @Getter
    private boolean submitted = false;
    @Getter
    private boolean lastHit = false;
    @Inject
    private HitCheckService service;
    @Inject
    private ResultsManager resultsManager;

    public void checkPoint() {
        try {
            HitResult result = service.checkHit(x, y, r);
            lastHit = result.isHit();
            submitted = true;
            resultsManager.addResult(result);
            logger.info("Result added: " + result);
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "Error", "An unexpected error occurred."
            ));
            submitted = false;
        }
    }

    public void handleCanvasClick() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        String xStr = params.get("clickedX");
        String yStr = params.get("clickedY");
        String rStr = params.get("clickedR");

        try {
            Double clickedX = xStr != null ? Double.parseDouble(xStr) : null;
            Double clickedY = yStr != null ? Double.parseDouble(yStr) : null;
            Double clickedR = rStr != null ? Double.parseDouble(rStr) : null;

            if (clickedX != null && clickedY != null && clickedR != null) {
                this.x = clickedX;
                this.y = clickedY;
                this.r = clickedR;

                checkPoint();
            } else {
                logger.warning("One or more parameters from canvas click are null or invalid: x=" + xStr + ", y=" + yStr + ", r=" + rStr);
                context.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error",
                        "Parameters from canvas click are invalid."
                ));
            }
        } catch (NumberFormatException e) {
            logger.severe("Failed to parse coordinates from canvas click: " + e.getMessage());
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Failed to parse coordinates from canvas click."
            ));
            e.printStackTrace();
        }
    }
}