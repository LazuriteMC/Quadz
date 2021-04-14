package dev.lazurite.quadz.common.data.model;

@io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Settings
public class Profile {
    public String name;
    public String template;

    public int cameraAngle;
    public float mass;
    public float dragCoefficient;
    public float thrust;
    public float thrustCurve;
    public float width;
    public float height;

    public Profile(String name, Settings settings) {
        this.name = name;
        this.template = settings.getId();
        this.cameraAngle = settings.getCameraAngle();
        this.mass = settings.getMass();
        this.dragCoefficient = settings.getDragCoefficient();
        this.thrust = settings.getThrust();
        this.thrustCurve = settings.getThrustCurve();
        this.width = settings.getWidth();
        this.height = settings.getHeight();
    }

    public Profile() {

    }
}
