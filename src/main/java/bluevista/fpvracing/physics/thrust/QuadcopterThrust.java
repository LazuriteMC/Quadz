package bluevista.fpvracing.physics.thrust;

import bluevista.fpvracing.client.input.InputTick;
import bluevista.fpvracing.config.Config;
import bluevista.fpvracing.server.entities.QuadcopterEntity;
import bluevista.fpvracing.util.Matrix4fInject;
import bluevista.fpvracing.util.math.BetaflightHelper;
import bluevista.fpvracing.util.math.QuaternionHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import javax.vecmath.Vector3f;

public class QuadcopterThrust implements IThrust {
    private QuadcopterEntity quad;

    public QuadcopterThrust(QuadcopterEntity quad) {
        this.quad = quad;
    }

    /**
     * Calculates the amount of force thrust should produce based on throttle and yaw input.
     * @return a {@link Vector3f} containing the direction and amount of force (in newtons)
     */
    public Vector3f getForce() {
        Vector3f thrustVec = getVector();
        thrustVec.scale(calculateCurve() * quad.getConfigValues(Config.THRUST).floatValue());

        Vector3f yawVec = getVector();
        yawVec.scale((float) Math.abs(BetaflightHelper.calculateRates(InputTick.axisValues.currY, quad.getConfigValues(Config.RATE).floatValue(), quad.getConfigValues(Config.EXPO).floatValue(), quad.getConfigValues(Config.SUPER_RATE).floatValue(), 1.0f)));

        Vector3f out = new Vector3f();
        out.add(thrustVec, yawVec);
        return out;
    }

    /**
     * Get the direction the bottom of the quad is facing.
     * @return {@link Vec3d} containing thrust direction
     */
    public Vector3f getVector() {
        QuaternionHelper.rotateX(quad.getPhysics().getOrientation(), 90);
        Matrix4f mat = new Matrix4f();
        Matrix4fInject.from(mat).fromQuaternion(QuaternionHelper.quat4fToQuaternion(quad.getPhysics().getOrientation()));

        Vector3f out = Matrix4fInject.from(mat).matrixToVector();
        out.scale(-1);
        return out;
    }

    /**
     * Calculates the thrust curve using a power between zero and one (one being perfectly linear).
     * @return a point on the thrust curve
     */
    public float calculateCurve() {
        return (float) (Math.pow(InputTick.axisValues.currT, quad.getConfigValues(Config.THRUST_CURVE).floatValue()));
    }

}
