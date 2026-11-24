package cl.clillo.lighting.external.midi;

/**
 * Bank A/B del APC Mini. Shift hace toggle; bank B multiplica los pads disponibles.
 */
public final class MidiShiftBank {

    public static final int BANK_A = 0;
    public static final int BANK_B = 1;

    private static final MidiShiftBank INSTANCE = new MidiShiftBank();

    private volatile int bank = BANK_A;

    private MidiShiftBank() {
    }

    public static MidiShiftBank getInstance() {
        return INSTANCE;
    }

    public int getBank() {
        return bank;
    }

    public boolean isBankB() {
        return bank == BANK_B;
    }

    public int toggle() {
        bank = isBankB() ? BANK_A : BANK_B;
        return bank;
    }
}
