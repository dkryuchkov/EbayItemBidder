package bidder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dmitry
 */
public class GumtreeOtherException extends Exception {

    public GumtreeOtherException(Exception ex) {
        super(ex);
    }

    public GumtreeOtherException(String ex) {
        super(ex);
    }

    public GumtreeOtherException() {
        super();
    }
}
