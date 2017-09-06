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
    // Fitbit Calories
    ACTIVITY{
        @Override
        public String toString() {
            return "You logged an activity on Fitbit: ";
        }
    }
}
