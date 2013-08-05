package com.yammer.homie.service.azure;

public enum RequestState {
    PENDING (){
        @Override
        public void visit(RequestStateVisitor visitor) {
            visitor.pending();
        }
    }
    , APPROVED(){
        @Override
        public void visit(RequestStateVisitor visitor) {
            visitor.approved();
        }
    }
    , DECLINED {
        @Override
        public void visit(RequestStateVisitor visitor) {
            visitor.declined();
        }
    };

    public abstract void visit(RequestStateVisitor visitor);

    public interface RequestStateVisitor {
        void declined();
        void approved();
        void pending();
    }
}
