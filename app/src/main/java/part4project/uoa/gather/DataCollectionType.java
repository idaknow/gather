package part4project.uoa.gather;

/**
 * Created by Ida on 1/08/2017.
 * This is an enum type containing all the data types specific to each application
 */

enum DataCollectionType {
    // Facebook Post
    FPOST{
        @Override
        public String toString() {
            return "You posted the status: ";
        }
    },
    // Facebook Event
    FEVENT{
        @Override
        public String toString() {
            return "You interacted with event: ";
        }
    },
    // Facebook Like
    FLIKE{
        @Override
        public String toString() {
            return "You liked: ";
        }
    },
    // Facebook Fitness Action
    FFITNESS{
        @Override
        public String toString() {
            return "You did fb fitness activity: ";
        }
    },
    // Twitter Tweet (could be favourite or status)
    TWEET{
        @Override
        public String toString() {
            return "You interacted with tweet: ";
        }
    },
    // Google Fit Calories
    GCALORIES{
        @Override
        public String toString() {
            return "Calories expended are: ";
        }
    },
    // Google Fit Nutrition
    GNUTRITION{
        @Override
        public String toString() {
            return "You ate the following food item: ";
        }
    },
    // Google Fit Hydration
    GHYDRATION{
        @Override
        public String toString() {
            return "Your water consumption (in liters): ";
        }
    },
    // Google Fit Step Count
    GSTEPS{
        @Override
        public String toString() {
            return "You did the following amount of steps: ";
        }
    },
    // Google Fit Activity
    GACTIVITY{
        @Override
        public String toString() {
            return "You did the following exercise: ";
        }
    },
    // Fitbit Logged Activity
    ACTIVITY{
        @Override
        public String toString() {
            return "You logged an activity on Fitbit: ";
        }
    },
    // Fitbit Logged Calories
    CALORIES{
        @Override
        public String toString() {
            return "Calories logged on Fitbit: ";
        }
    }
}
