public String getMessage() { Document document; String message;
                try { document = m_config.getDocument();
                }
                catch(Throwable t)  {
                        t.printStackTrace(); message = t.getMessage(); }
                return (message);}
