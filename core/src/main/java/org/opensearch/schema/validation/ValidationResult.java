package org.opensearch.schema.validation;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;
import org.opensearch.schema.ontology.Printable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationResult {
    public static ValidationResult OK = new ValidationResult(true, "none");

    public static String print(Object... elements) {
        StringJoiner joiner = new StringJoiner(":", "[", "]");
        for (Object element : elements) {
            if (Printable.class.isAssignableFrom(element.getClass()))
                ((Printable) element).print(joiner);
            else
                joiner.add(element.toString());
        }
        return joiner.toString();
    }

    //region Constructors

    public ValidationResult() {
    }

    public ValidationResult(boolean valid, String validator, String... errors) {
        this(valid, validator, Stream.of(errors));
    }

    public ValidationResult(boolean valid, String validator, Iterable<String> errors) {
        this.valid = valid;
        this.validator = validator;
        this.errors = Stream.ofAll(errors).toJavaList();
    }
    //endregion

    //region Public Methods
    public boolean valid() {
        return valid;
    }

    public Iterable<String> errors() {
        return errors;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        if (valid())
            return "valid";
        return print(errors + ":" + validator);
    }

    public String getValidator() {
        return validator;
    }
//endregion

    //region Fields
    @JsonProperty("validator")
    private String validator;
    @JsonProperty("isValid")
    private boolean valid;
    @JsonProperty("errors")
    private Iterable<String> errors;
    //endregion

    public static class ValidationResults {
        private List<ValidationResult> validations = new ArrayList<>();

        public ValidationResults() {
        }

        public ValidationResults( List<ValidationResult> validations) {
            this.validations = validations;
        }

        public boolean isValid() {
            return getValidations().isEmpty();
        }

        public List<ValidationResult> getValidations() {
            return validations;
        }

        public ValidationResults with(ValidationResult validation) {
            this.validations.add(validation);
            return this;
        }
    }
}
