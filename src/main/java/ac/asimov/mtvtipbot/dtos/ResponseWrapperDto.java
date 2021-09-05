package ac.asimov.mtvtipbot.dtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResponseWrapperDto<T> {
    private T response;
    private String errorMessage;
    private List<ResponseErrorDto> errors;
    private List<ResponseWarningDto> warnings;
    private List<ResponseSuccessDto> successes;

    public ResponseWrapperDto() {
        this.errors = new ArrayList();
        this.warnings = new ArrayList();
        this.successes = new ArrayList();
    }

    public ResponseWrapperDto(ResponseErrorDto error) {
        this.errors = new ArrayList();
        this.warnings = new ArrayList();
        this.successes = new ArrayList();
        this.errors.add(error);
    }

    public ResponseWrapperDto(String errorMessage) {
        this.errors = new ArrayList();
        this.warnings = new ArrayList();
        this.successes = new ArrayList();
        this.errors.add(new ResponseErrorDto(errorMessage));
    }

    public ResponseWrapperDto(T responseObject) {
        this();
        this.response = responseObject;
    }

    public ResponseWrapperDto(List<String> errorMessages) {
        this.errors = new ArrayList();
        this.warnings = new ArrayList();
        this.successes = new ArrayList();
        errorMessages.stream().forEach((e) -> {
            this.errors.add(new ResponseErrorDto(e));
        });
    }

    public boolean hasErrors() {
        return this.errors != null && this.errors.size() > 0;
    }

    public void addError(ResponseErrorDto error) {
        this.errors.add(error);
    }

    public void addErrors(List<ResponseErrorDto> errors) {
        this.errors.addAll(errors);
    }

    public void addErrorMessage(String error) {
        this.errors.add(new ResponseErrorDto(error));
    }

    public void addErrorMessages(List<String> errors) {
        Iterator var2 = errors.iterator();

        while(var2.hasNext()) {
            String s = (String)var2.next();
            this.errors.add(new ResponseErrorDto(s));
        }

    }

    public List<ResponseErrorDto> getErrors() {
        return this.errors;
    }

    public void setErrors(List<ResponseErrorDto> errors) {
        this.errors = errors;
    }

    public String getErrorMessage() {
        if (this.errors.size() <= 1) {
            return this.errors.size() == 1 ? ((ResponseErrorDto)this.errors.get(0)).getMessage() : "";
        } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < this.errors.size() - 1; ++i) {
                sb.append(((ResponseErrorDto)this.errors.get(i)).getMessage()).append("<br />");
            }

            sb.append(((ResponseErrorDto)this.errors.get(this.errors.size() - 1)).getMessage());
            return sb.toString();
        }
    }

    public boolean hasWarnings() {
        return this.warnings != null && this.warnings.size() > 0;
    }

    public void addWarning(ResponseWarningDto warning) {
        this.warnings.add(warning);
    }

    public void addWarnings(List<ResponseWarningDto> warnings) {
        this.warnings.addAll(warnings);
    }

    public void addWarningMessage(String warning) {
        this.warnings.add(new ResponseWarningDto(warning));
    }

    public void addWarningMessages(List<String> warnings) {
        Iterator var2 = warnings.iterator();

        while(var2.hasNext()) {
            String s = (String)var2.next();
            this.warnings.add(new ResponseWarningDto(s));
        }

    }

    public List<ResponseWarningDto> getWarnings() {
        return this.warnings;
    }

    public void setWarnings(List<ResponseWarningDto> warnings) {
        this.warnings = warnings;
    }

    public String getWarningMessage() {
        if (this.warnings.size() <= 1) {
            return this.warnings.size() == 1 ? ((ResponseWarningDto)this.warnings.get(0)).getMessage() : "";
        } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < this.warnings.size() - 1; ++i) {
                sb.append(((ResponseWarningDto)this.warnings.get(i)).getMessage()).append("<br />");
            }

            sb.append(((ResponseWarningDto)this.warnings.get(this.warnings.size() - 1)).getMessage());
            return sb.toString();
        }
    }

    public boolean hasSuccess() {
        return this.successes != null && this.successes.size() > 0;
    }

    public void addSuccess(ResponseSuccessDto success) {
        this.successes.add(success);
    }

    public void addSuccesses(List<ResponseSuccessDto> successes) {
        this.successes.addAll(successes);
    }

    public void addSuccessMessage(String success) {
        this.successes.add(new ResponseSuccessDto(success));
    }

    public void addSuccessMessages(List<String> successes) {
        Iterator var2 = successes.iterator();

        while(var2.hasNext()) {
            String s = (String)var2.next();
            this.successes.add(new ResponseSuccessDto(s));
        }

    }

    public List<ResponseSuccessDto> getSuccesses() {
        return this.successes;
    }

    public void setSuccesses(List<ResponseSuccessDto> successes) {
        this.successes = successes;
    }

    public String getSuccessMessage() {
        if (this.successes.size() <= 1) {
            return this.successes.size() == 1 ? ((ResponseSuccessDto) this.successes.get(0)).getMessage() : "";
        } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < this.successes.size() - 1; ++i) {
                sb.append(((ResponseSuccessDto) this.successes.get(i)).getMessage()).append("<br />");
            }

            sb.append(((ResponseSuccessDto) this.successes.get(this.successes.size() - 1)).getMessage());
            return sb.toString();
        }
    }

    public T getResponse() {
        return this.response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
