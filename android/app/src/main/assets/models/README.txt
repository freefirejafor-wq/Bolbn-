PENALTY ARENA - 3D RIGGED MODEL ASSET SPECIFICATIONS
=====================================================

To load custom GLB / glTF 2.0 rigged models into Penalty Arena:

1. Striker Rigged Model:
   - File: striker_rigged.glb
   - Hierarchy: Standard humanoid humanoid biped skeleton (Hips, Spine, Chest, Neck, Head, LeftUpLeg, RightUpLeg, LeftLeg, RightLeg, LeftFoot, RightFoot, LeftArm, RightArm).
   - Animation Clips (case-sensitive):
     * "idle" (looping stance, weight shifting)
     * "run_up" (2-3 acceleration strides toward penalty spot)
     * "kick" (plant foot, follow-through swing)
     * "celebration" (fist pump / knee slide)
     * "disappointed" (hands on head / looking at turf)

2. Goalkeeper Rigged Model:
   - File: goalkeeper_rigged.glb
   - Hierarchy: Matching standard humanoid biped with glove mesh weights.
   - Animation Clips:
     * "idle" (goalkeeper bounce, knees flexed, hands forward)
     * "dive_left" (lateral explosive dive to bottom/top left)
     * "dive_right" (lateral explosive dive to bottom/top right)
     * "dive_center" (upward leap or reflex punch)
     * "save_celebration" (pumping fists after a crucial save)
     * "disappointed" (conceding a goal, picking ball up)

3. Fallback Engine:
   - When .glb files are not present, the game automatically switches to the
     built-in High-Performance 2.5D Compose Canvas Engine. This features realistic
     stadium floodlights, perspective goal net, animated skeletal joints, ball trajectory,
     and particle effects on post/bar hits.
