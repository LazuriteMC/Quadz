package bluevista.fpvracing.physics.entity;

import bluevista.fpvracing.network.packets.EntityPhysicsS2C;
import bluevista.fpvracing.server.entities.FlyableEntity;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class ServerEntityPhysics implements IEntityPhysics {
    private final FlyableEntity entity;
    private final Vector3f position;
    private final Vector3f linearVelocity;
    private final Vector3f angularVelocity;
    private final Quat4f orientation;

    public ServerEntityPhysics(FlyableEntity entity) {
        this.entity = entity;

        position = new Vector3f();
        linearVelocity = new Vector3f();
        angularVelocity = new Vector3f();
        orientation = new Quat4f();
    }

    @Override
    public void sendPackets() {
        EntityPhysicsS2C.send(this, false);
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position.set(position);
        this.entity.updatePosition(position.x, position.y, position.z);
    }

    @Override
    public Vector3f getPosition() {
        return this.position;
    }

    @Override
    public void setLinearVelocity(Vector3f linearVelocity) {
        this.linearVelocity.set(linearVelocity);
    }

    @Override
    public Vector3f getLinearVelocity() {
        return this.linearVelocity;
    }

    @Override
    public void setAngularVelocity(Vector3f angularVelocity) {
        this.angularVelocity.set(angularVelocity);
    }

    @Override
    public Vector3f getAngularVelocity() {
        return this.angularVelocity;
    }

    @Override
    public void setOrientation(Quat4f orientation) {
        this.orientation.set(orientation);
    }

    @Override
    public Quat4f getOrientation() {
        return this.orientation;
    }

    @Override
    public FlyableEntity getEntity() {
        return entity;
    }
}
