package dev.lazurite.fpvracing.physics.thrust;

import dev.lazurite.fpvracing.client.input.InputTick;
import dev.lazurite.fpvracing.server.entity.flyable.QuadcopterEntity;
import dev.lazurite.fpvracing.util.Matrix4fInject;
import dev.lazurite.fpvracing.util.math.BetaflightHelper;
import dev.lazurite.fpvracing.util.math.QuaternionHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

public class QuadcopterThrust implements Thrust {
    private final QuadcopterEntity quad;

    public QuadcopterThrust(QuadcopterEntity quad) {
        this.quad = quad;
    }

    /**
     * Calculates the amount of force thrust should produce based on throttle and yaw input.
     * @return a {@link Vector3f} containing the direction and amount of force (in newtons)
     */
    public Vector3f getForce() {
        Vector3f thrustVec = getVector();
        thrustVec.scale(calculateCurve() * quad.getValue(QuadcopterEntity.THRUST));

        Vector3f yawVec = getVector();
        yawVec.scale((float) Math.abs(BetaflightHelper.calculateRates(InputTick.axisValues.currY, quad.getValue(QuadcopterEntity.RATE), quad.getValue(QuadcopterEntity.EXPO), quad.getValue(QuadcopterEntity.SUPER_RATE), 0.01f)));

        Vector3f out = new Vector3f();
        out.add(thrustVec, yawVec);
        out.negate();
        return out;
    }

    /**
     * Get the direction the bottom of the quad is facing.
     * @return {@link Vec3d} containing thrust direction
     */
    public Vector3f getVector() {
        Quat4f orientation = quad.getPhysics().getOrientation();
        QuaternionHelper.rotateX(orientation, 90);
        Matrix4f mat = new Matrix4f();
        Matrix4fInject.from(mat).fromQuaternion(QuaternionHelper.quat4fToQuaternion(orientation));

        return Matrix4fInject.from(mat).matrixToVector();
    }

    /**
     * Calculates the thrust curve using a power between zero and one (one being perfectly linear).
     * @return a point on the thrust curve
     */
    public float calculateCurve() {
        return (float) (Math.pow(InputTick.axisValues.currT, quad.getValue(QuadcopterEntity.THRUST_CURVE)));
    }
}
