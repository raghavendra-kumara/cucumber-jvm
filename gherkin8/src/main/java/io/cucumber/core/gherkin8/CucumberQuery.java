package io.cucumber.core.gherkin8;

import io.cucumber.messages.Messages.GherkinDocument;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild.RuleChild;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Scenario.Examples;
import io.cucumber.messages.Messages.GherkinDocument.Feature.Step;
import io.cucumber.messages.Messages.GherkinDocument.Feature.TableRow;
import io.cucumber.messages.Messages.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class CucumberQuery {
    private final Map<String, Step> gherkinStepById = new HashMap<>();
    private final Map<String, Scenario> gherkinScenarioById = new HashMap<>();
    private final Map<String, Location> locationBySourceId = new HashMap<>();

    public void update(GherkinDocument gherkinDocument) {
        for (FeatureChild featureChild : gherkinDocument.getFeature().getChildrenList()) {
            if (featureChild.hasBackground()) {
                this.updateBackground(
                    featureChild.getBackground(),
                    gherkinDocument.getUri()
                );
            }

            if (featureChild.hasScenario()) {
                this.updateScenario(
                    featureChild.getScenario(),
                    gherkinDocument.getUri()
                );
            }

            if (featureChild.hasRule()) {
                for (RuleChild ruleChild : featureChild.getRule().getChildrenList()) {
                    if (ruleChild.hasBackground()) {
                        this.updateBackground(
                            ruleChild.getBackground(),
                            gherkinDocument.getUri()
                        );
                    }

                    if (ruleChild.hasScenario()) {
                        this.updateScenario(
                            ruleChild.getScenario(),
                            gherkinDocument.getUri()
                        );
                    }
                }
            }
        }
    }

    private void updateScenario(Scenario scenario, String uri) {
        gherkinScenarioById.put(requireNonNull(scenario.getId()), scenario);
        locationBySourceId.put(requireNonNull(scenario.getId()), scenario.getLocation());
        updateStep(scenario.getStepsList());

        for (Examples examples: scenario.getExamplesList()) {
            for (TableRow tableRow: examples.getTableBodyList()) {
                this.locationBySourceId.put(requireNonNull(tableRow.getId()), tableRow.getLocation());
            }
        }
    }

    private void updateBackground(GherkinDocument.Feature.Background background, String uri) {
        updateStep(background.getStepsList());
    }

    private void updateStep(List<Step> stepsList) {
        for (Step step : stepsList) {
            locationBySourceId.put(requireNonNull(step.getId()), step.getLocation());
            gherkinStepById.put(requireNonNull(step.getId()), step);
        }
    }

    public Step getGherkinStep(String id) {
        return requireNonNull(gherkinStepById.get(requireNonNull(id)));
    }

    public Scenario getGherkinScenario(String id) {
        return requireNonNull(gherkinScenarioById.get(requireNonNull(id)));
    }

    public Location getLocation(String sourceId) {
        Location location = locationBySourceId.get(requireNonNull(sourceId));
        return requireNonNull(location);
    }
}