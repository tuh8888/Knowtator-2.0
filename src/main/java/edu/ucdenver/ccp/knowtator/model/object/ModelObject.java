/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.model.object;

import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;

/**
 * The interface Model object.
 *
 * @param <K> the type parameter
 */
public interface ModelObject<K extends ModelObject> extends Comparable<K> {

  /**
   * Gets id.
   *
   * @return the id
   */
  String getId();

  /**
   * Sets id.
   *
   * @param id the id
   */
  void setId(String id);

  /**
   * Extract int int.
   *
   * @param s the s
   * @return the int
   */
  static int extractInt(String s) {
    String num = s.replaceAll("\\D", "");
    // return 0 if no digits found
    try {
      return num.isEmpty() ? 0 : Integer.parseInt(num);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /** Dispose. */
  void dispose();

  /**
   * Gets knowtator model.
   *
   * @return the knowtator model
   */
  KnowtatorModel getKnowtatorModel();

  @Override
  default int compareTo(K o) {
    if (this == o) {
      return 0;
    }
    if (o == null) {
      return 1;
    }

    int result = extractInt(this.getId()) - extractInt(o.getId());
    if (result == 0) {
      return this.getId().compareTo(o.getId());
    } else {
      return result;
    }
  }
}
