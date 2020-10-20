package bluevista.fpvracing.physics.entity;

import bluevista.fpvracing.server.entities.FlyableEntity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public interface IEntityPhysics {
    void sendPackets();

    Vector3f getPosition();
    Vector3f getLinearVelocity();
    Vector3f getAngularVelocity();
    Quat4f getOrientation();
    FlyableEntity getEntity();

    void setPosition(Vector3f position);
    void setLinearVelocity(Vector3f linearVelocity);
    void setAngularVelocity(Vector3f angularVelocity);
    void setOrientation(Quat4f orientation);
}
