package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.api.utils.LocationUtils.hasOtherThanExtended;
import static com.elikill58.negativity.universal.CheatKeys.FLY;
import static com.elikill58.negativity.universal.utils.UniversalUtils.parseInPorcent;

import java.util.List;

import com.elikill58.negativity.api.GameMode;
import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.block.BlockFace;
import com.elikill58.negativity.api.entity.EntityType;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Material;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.potion.PotionEffect;
import com.elikill58.negativity.api.potion.PotionEffectType;
import com.elikill58.negativity.api.utils.LocationUtils;
import com.elikill58.negativity.api.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.Version;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class Fly extends Cheat implements Listeners {

	public Fly() {
		super(FLY, CheatCategory.MOVEMENT, Materials.FIREWORK, true, false, "flyhack");
	}

	@EventListener
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this) || e.isCancelled())
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		ItemStack hand = p.getItemInHand();
		if (hand != null && hand.getType().getId().contains("TRIDENT"))
			return;
		if(Version.getVersion().isNewerOrEquals(Version.V1_9) && p.hasPotionEffect(PotionEffectType.LEVITATION))
			return;
		if (p.getAllowFlight() || p.isSwimming())
			return;
		if (p.getPotionEffect(PotionEffectType.SPEED).orElseGet(() -> new PotionEffect(PotionEffectType.SPEED)).getAmplifier() > 5)
			return;

		Location from = e.getFrom(), to = e.getTo();
		
		boolean mayCancel = false, inBoat = Utils.isInBoat(p);
		double y = from.getY() - to.getY();
		Location loc = p.getLocation().clone(),
				locUnder = p.getLocation().clone().sub(0, 1, 0),
				locUnderUnder = p.getLocation().clone().sub(0, 2, 0);
		Material type = loc.getBlock().getType(), typeUpper = loc.getBlock().getRelative(BlockFace.UP).getType();
		boolean isInWater = loc.getBlock().getType().getId().contains("WATER"), isOnWater = locUnder.getBlock().getType().getId().contains("WATER");
		
		double i = to.toVector().distance(from.toVector());
		double d = to.getY() - from.getY();
		double distance = from.distance(to);
		
		if(p.hasElytra()) {
			if(checkActive("elytra")) {
				boolean isUsingFireworks = np.booleans.get(FLY, "fireworks", false);
				boolean isGoingDown = y > 0;
				if(isGoingDown) {
					np.booleans.remove(FLY, "fireworks");
				} else if(!isUsingFireworks) {
					if(hand != null && hand.getType().equals(Materials.FIREWORK)) {
						np.booleans.set(FLY, "fireworks", true);
					} else {
						if(distance > 0.29)
							Negativity.alertMod(ReportType.WARNING, p, this, 99, "elytra", "Going UP, with in hand: " + hand + ". Y: " + y + ", distance: " + distance, new CheatHover.Literal("Elytra fly"));
					}
				}
			}
		} else {
			if(checkActive("suspicious-y")) {
				boolean hasBuggedBlockAroundForGeyser = np.isBedrockPlayer() && LocationUtils.hasMaterialsAround(locUnder, "SLAB", "FENCE", "STAIRS", "BED");
				String strY = String.valueOf(y);
				if(strY.contains("E") && !strY.equalsIgnoreCase("2.9430145066276694E-4") && !p.isInsideVehicle()
						&& !np.isInFight && !LocationUtils.hasBoatAroundHim(p.getLocation()) && !(isInWater || isOnWater)
						&& !LocationUtils.hasMaterialsAround(loc, "SCAFFOLD", "LAVA", "WATER") && !inBoat && !hasBuggedBlockAroundForGeyser){
					mayCancel = Negativity.alertMod(np.getWarn(this) > 5 ? ReportType.VIOLATION : ReportType.WARNING,
								p, this, 97, "suspicious-y", "Suspicious Y: " + y);
				}
			}
			if(checkActive("no-ground-i")) {
				if (!(p.isSprinting() && d > 0)
						&& locUnder.getBlock().getType().equals(Materials.AIR)
						&& locUnderUnder.getBlock().getType().equals(Materials.AIR)
						&& (p.getFallDistance() == 0.0F || inBoat)
						&& typeUpper.equals(Materials.AIR) && i > 0.8
						&& !p.isOnGround()) {
					mayCancel = Negativity.alertMod(np.getWarn(this) > 5 ? ReportType.VIOLATION : ReportType.WARNING, p,
							this, parseInPorcent((int) i * 50), "no-ground-i",
							"Player not in ground, i: " + i + ", onBoat: " + inBoat + ", distanceYToFrom: " + d,
							inBoat ? hoverMsg("boat") : null);
				}
			}
			
			if(checkActive("no-ground-down") && !np.booleans.get("ALL", "jump-boost-use", false)) {
				if (!np.isUsingSlimeBlock && !hasOtherThanExtended(p.getLocation(), "AIR")
						&& !hasOtherThanExtended(locUnder, "AIR") && !np.booleans.get(FLY, "boat-falling", false)
						&& !hasOtherThanExtended(locUnderUnder, "AIR") && d != 0.5 && d != 0
						&& (from.getY() <= to.getY() || inBoat) && p.getVelocity().length() < 1.5) {
					double nbTimeAirBelow = np.doubles.get(FLY, "air-below", 0.0);
					np.doubles.set(FLY, "air-below", nbTimeAirBelow + 1);
					if(nbTimeAirBelow > 6) { // we don't care when player jump
						int nb = LocationUtils.getNbAirBlockDown(p), porcent = parseInPorcent(nb * 15 + d);
						if (LocationUtils.hasOtherThan(p.getLocation().add(0, -3, 0), Materials.AIR))
							porcent = parseInPorcent(porcent - 15);
						mayCancel = Negativity.alertMod(np.getWarn(this) > 5 ? ReportType.VIOLATION : ReportType.WARNING, p,
								this, porcent, "no-ground-down", "Player not in ground (" + nb + " air blocks down), distance Y: " + d,
										hoverMsg(inBoat ? "boat_air_below" : "air_below", "%nb%", nb));
					}
				} else
					np.doubles.remove(FLY, "air-below");
			}
			
			if(checkActive("no-ground-y")) {
				to = to.clone();
				to.setY(from.getY());
				double distanceWithoutY = to.distance(from);
				if (distanceWithoutY == i && !p.isOnGround() && i != 0
						&& typeUpper.equals(Materials.AIR) && !p.isInsideVehicle()
						&& !type.getId().contains("WATER") && distanceWithoutY > 0.3) {
					if (np.booleans.get(FLY, "not-moving-y", false))
						mayCancel = Negativity.alertMod(np.getWarn(this) > 5 ? ReportType.VIOLATION : ReportType.WARNING,
								p, this, 98, "no-ground-y", "Player not in ground but not moving Y. DistanceWithoutY: " + distanceWithoutY);
					np.booleans.set(FLY, "not-moving-y", true);
				} else
					np.booleans.remove(FLY, "not-moving-y");
			}
			if(checkActive("not-moving-y")) {
				if(p.isOnGround() && y == 0 && locUnder.getBlock().getType().equals(Materials.AIR) && distance > 0.1 && !LocationUtils.hasOtherThan(locUnder, Materials.AIR)) {
					int time0 = np.ints.get(FLY, "y-0-times", 0);
					if(time0 > 2) {
						mayCancel = Negativity.alertMod(time0 > 10 ? ReportType.VIOLATION : ReportType.WARNING, p, this, parseInPorcent(time0 * 30), "not-moving-y", "Times not moving Y and on ground: " + time0 + ", distance: " + distance, null, time0 < 3 ? 1 : time0 - 2);
					}
					np.ints.set(FLY, "y-0-times", time0 + 1);
				} else
					np.ints.remove(FLY, "y-0-times");
			}
		}
		if (isSetBack() && mayCancel) {
			LocationUtils.teleportPlayerOnGround(p);
		}
	}
	

	@EventListener
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		if (!np.hasDetectionActive(this) || e.isCancelled() || !checkActive("omega-craft"))
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		if(np.isUsingSlimeBlock)
			return;
		boolean onGround = p.isOnGround(), wasOnGround = np.booleans.get(FLY, "fly-wasOnGround", true);
		double y = e.getTo().getY() - e.getFrom().getY();
		List<Double> list = np.flyMoveAmount;
		if(p.getFallDistance() <= 0.000001 && list.size() > 1) {
			int size = list.size();
			int amount = 0;
			for(int i = 1; i < size - 1; i++) {
				double last = list.get(i - 1);
				double current = list.get(i);
				if((last + current) == 0) {
					if(i < (size - 2)) {
						double next = list.get(i + 1);
						if((current + next) == 0) {
							amount++;
						}
					} else
						amount++;
				}
			}
			if(amount > 0) {
				Negativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(90 + amount), "OmegaCraftFly - " + list.size() + " > " + onGround + " : " + wasOnGround, "omega-craft", (CheatHover) null, amount > 1 ? amount - 1 : 1);
			}
		}
		if((onGround && wasOnGround) || (y > 0.1 || y < -0.1) || LocationUtils.hasMaterialsAround(e.getTo(), "FENCE", "SLIME"))
			list.clear();
		else
			list.add(y);
		np.booleans.set(FLY, "fly-wasOnGround", onGround);
	}

	@EventListener
	public void boatManager(PlayerMoveEvent e) {
		if(!checkActive("no-ground-down"))
			return;
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		boolean nextValue = np.booleans.get(FLY, "boat-falling", false);
		if(p.isInsideVehicle() && p.getVehicle().getType().equals(EntityType.BOAT)) {
			Location from = e.getFrom().clone(), to = e.getTo().clone();
			double moveY = (to.getY() - from.getY());
			
			boolean wasWaterBelow = from.sub(0, 1, 0).getBlock().getType().getId().contains("WATER");
			boolean willWaterBelow = to.sub(0, 1, 0).getBlock().getType().getId().contains("WATER");
			if(wasWaterBelow && !willWaterBelow)
				nextValue = true;
			
			if(nextValue && !willWaterBelow && moveY >= 0)
				nextValue = false;
		} else {
			if(!nextValue)
				return; // already set to false, don't need to save it while put it in map
			nextValue = false;
		}
		
		np.booleans.set(FLY, "boat-falling", nextValue);
	}

	@Override
	public boolean isBlockedInFight() {
		return true;
	}
}