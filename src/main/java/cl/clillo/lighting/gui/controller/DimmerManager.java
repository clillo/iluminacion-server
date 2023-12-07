package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.external.midi.KeyData;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.repository.StateRepository;

import javax.swing.JSlider;
import java.util.ArrayList;
import java.util.List;

public class DimmerManager {

    String []types = {"RGBW", "Moving Head", "Moving Head", "Moving Head"};
    String []models = {"Generic", "beam+spot", "Moving Head 2", "Moving Head Spot"};

    private final Dmx dmx = Dmx.getInstance();
    private final JSlider sldrMasterDimmer;
    private final int[] masterDimmerChannels;
    private final StateRepository stateRepository = StateRepository.getInstance();
    private final int index;

    public DimmerManager(final JSlider sldrMasterDimmer, final int index) {
        this.sldrMasterDimmer = sldrMasterDimmer;
        this.index = index;
        final List<Integer> channels = new ArrayList<>();
        final ShowCollection showCollection = ShowCollection.getInstance();
        final List<QLCFixture> fixtureList = showCollection.getQlcModel().getFixtureList();

        for (QLCFixture fixture : fixtureList) {
            int dmxMasterDimmer = fixture.getDMXDimmerChannel();
            if (dmxMasterDimmer > 0
                    && types[index].equals(fixture.getFixtureModel().getType())
                    && models[index].equals(fixture.getFixtureModel().getModel())) {
                channels.add(dmxMasterDimmer);
             }
        }

        masterDimmerChannels = new int[channels.size()];
        int i = 0;
        for (int dmx : channels)
            masterDimmerChannels[i++] = dmx+2;

        sldrMasterDimmer.setValue(getRepositorySliderValue(index));
        setRepositorySliderValue(index, sldrMasterDimmer.getValue());

    }

    public void onSlide(final KeyData keyData, final JSlider slider) {
        double value = keyData.getSliderValue() * 255.0;

        slider.setValue((int) value);
        if (slider == sldrMasterDimmer)
            adjustMasterDimmer();
    }

    public void adjustMasterDimmer() {
        setRepositorySliderValue(index, sldrMasterDimmer.getValue());

        for (int dmxMasterDimmer : masterDimmerChannels) {
            dmx.send(dmxMasterDimmer, sldrMasterDimmer.getValue());
        }
    }

    public void save() {
        setRepositorySliderValue(index, sldrMasterDimmer.getValue());
        stateRepository.write();
    }

    private int getRepositorySliderValue(int index){
        switch (index){
            case 0:
                return stateRepository.getRgbwMasterDimmer();
            case 1:
                return stateRepository.getMovingHeadSpotBeamMasterDimmer();
            case 2:
                return stateRepository.getMovingHeadBeamMasterDimmer();
            case 3:
                return stateRepository.getMovingHeadSpotMasterDimmer();
        }
        return -1;
    }

    private void setRepositorySliderValue(int index, int value){
        switch (index){
            case 0:
                stateRepository.setRgbwMasterDimmer(value, masterDimmerChannels);
                break;
            case 1:
                stateRepository.setMovingHeadSpotBeamMasterDimmer(value, masterDimmerChannels);
                break;
            case 2:
                stateRepository.setMovingHeadBeamMasterDimmer(value, masterDimmerChannels);
                break;
            case 3:
                stateRepository.setMovingHeadSpotMasterDimmer(value, masterDimmerChannels);
        }

    }
}