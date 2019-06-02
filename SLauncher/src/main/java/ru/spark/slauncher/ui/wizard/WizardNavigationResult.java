package ru.spark.slauncher.ui.wizard;

public enum WizardNavigationResult {
    PROCEED {
        @Override
        public boolean getDeferredComputation() {
            return true;
        }
    },
    DENY {
        @Override
        public boolean getDeferredComputation() {
            return false;
        }
    };

    public abstract boolean getDeferredComputation();
}
