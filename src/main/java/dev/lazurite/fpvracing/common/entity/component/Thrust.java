package dev.lazurite.fpvracing.common.entity.component;

public interface Thrust {
    void setThrustForce(float thrust);
    float getThrustForce();

    void setThrustCurve(float thrustCurve);
    float getThrustCurve();
}
