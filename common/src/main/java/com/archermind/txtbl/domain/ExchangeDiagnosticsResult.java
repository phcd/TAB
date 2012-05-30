package com.archermind.txtbl.domain;

public class ExchangeDiagnosticsResult {
    public enum DiagnosticsResult {
        SUCCESS("Successful"), FAILURE("Failure"), NOT_TESTED("Not tested");
        private String description;

        DiagnosticsResult(String description) {
            this.description = description;
        }

        public String toString() {
            return description;
        }
    }

    private ExchangeDiagnostics diagnostic;
    private DiagnosticsResult result;
    private String comment;

    public ExchangeDiagnosticsResult()
    {
    }

    public ExchangeDiagnosticsResult(ExchangeDiagnostics diagnostic, DiagnosticsResult result) {
        this.diagnostic = diagnostic;
        this.result = result;
    }

    public ExchangeDiagnostics getDiagnostic() {
        return diagnostic;
    }

    public void setDiagnostic(ExchangeDiagnostics diagnostic) {
        this.diagnostic = diagnostic;
    }

    public DiagnosticsResult getResult() {
        return result;
    }

    public void setResult(DiagnosticsResult result) {
        this.result = result;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
