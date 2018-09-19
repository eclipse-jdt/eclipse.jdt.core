class Example {

    a                    b;
    a                    b              = c;
    int                  i;
    int                  j              = 5;
    private String       someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
            + "12345678901234567890";
    private final Object someObjetc     = new Object() {
                                            @Override
                                            public String toString() {
                                                return super.toString();
                                            }
                                        };

    void variables() {
        a            b;
        a            b              = c;
        int          i;
        int          j              = 5;
        String       someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        final Object someObjetc     = new Object() {
                                        @Override
                                        public String toString() {
                                            return super.toString();
                                        }
                                    };
    }

    void variablesReordered() {
        a            b              = c;
        String       someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        int          j              = 5;
        final Object someObjetc     = new Object() {
                                        @Override
                                        public String toString() {
                                            return super.toString();
                                        }
                                    };
        int          i;
        a            b;
    }

    void variablesGaps() {
        a            b              = c;
        String       someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";

        int          j              = 5;
        final Object someObjetc     = new Object() {
                                        @Override
                                        public String toString() {
                                            return super.toString();
                                        }
                                    };

        // big gap

        int          i;
        a            b;
    }

    void variablesComments() {
        a /* c1 */             b;                                                                              // c1
        a /* c123 */           b /* c123 */           = /* c123 */ c;                                          // c123
        int /* */              i;                                                                              /* */
        int                    j                      = 5;                                                     /* */
        String /* c */         someLongString /* c */ = /* c */ "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890" + "12345678901234567890";
        /* ... */ final Object someObjetc             = new Object() {
                                                          @Override
                                                          public String toString() {
                                                              return super.toString();
                                                          }
                                                      };
    }

    void variablesAnnotations() {
        @SuppressWarnings
        a            b              = c;
        @Target
        String       someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        int          j              = 5;
        @Deprecated
        final Object someObjetc     = new Object() {
                                        @Override
                                        public String toString() {
                                            return super.toString();
                                        }
                                    };
        int          i;
        @Test
        a            b;
    }

    void assignments() {
        b                = c;
        j                = 5;
        myInteger        = 56436345;
        myOtehrInteger >>= 35534525543;
        someLongString   = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        someObjetc       = new Object() {
                             @Override
                             public String toString() {
                                 return super.toString();
                             }
                         };
    }

    void assignmentsReordered() {
        someLongString   = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        b                = c;
        myInteger        = 56436345;
        someObjetc       = new Object() {
                             @Override
                             public String toString() {
                                 return super.toString();
                             }
                         };
        myOtehrInteger >>= 35534525543;
        j                = 5;
    }

    void assignmentsGaps() {
        b                = c;
        myInteger        = 56436345;

        myOtehrInteger >>= 35534525543;
        j                = 5;

        // big gap

        someObjetc       = new Object() {
                             @Override
                             public String toString() {
                                 return super.toString();
                             }
                         };
        someLongString   = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
    }

    void assignmentsComments() {
        /* c1 */ b /* c1 */    = /* c1 */c;                                                              // c1
        j /* */                = /* c12345 */5;                                                          // c12345
        myInteger /* */        = 56436345;
        /* */ myOtehrInteger >>= 35534525543;
        someLongString         = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";                                                                /* ... */
        someObjetc             = /* !!! */ new Object() {                                                // !!!
                                   @Override
                                   public String toString() {
                                       return super.toString();                                          // !!!
                                   }
                               };
    }

    void mixed() {
        a   b;
        a   b = c;
        int i;
        j              = 5;
        someLongString = "12345678901234567890" + "12345678901234567890" + "12345678901234567890"
                + "12345678901234567890";
        final Object someObjetc = new Object() {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
}