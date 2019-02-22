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

import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXmlUtil;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.Savable;
import edu.ucdenver.ccp.knowtator.view.KnowtatorDefaultSettings;
import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;

/** The type Profile. */
public class Profile implements ModelObject<Profile>, Savable {
  @SuppressWarnings("unused")
  private static Logger log = LogManager.getLogger(Profile.class);

  private String id;
  private final HashMap<OWLClass, Color> colors; // <ClassName, Highlighter>
  private final KnowtatorModel model;

  /**
   * Instantiates a new Profile.
   *
   * @param model the model
   * @param id the id
   */
  public Profile(KnowtatorModel model, String id) {
    colors = new HashMap<>();
    this.model = model;
    model.verifyId(id, this, false);
  }

  /*
  COMPARISON
   */

  @Override
  public int compareTo(Profile profile2) {
    if (this == profile2) {
      return 0;
    }
    if (profile2 == null) {
      return 1;
    }
    return this.getId().toLowerCase().compareTo(profile2.getId().toLowerCase());
  }

  /*
  GETTERS
   */

  @Override
  public String getId() {
    return id;
  }

  /**
   * Gets color.
   *
   * @param owlClass the owl class
   * @return the color
   */
  public Color getColor(OWLClass owlClass) {
    return colors.getOrDefault(owlClass, KnowtatorDefaultSettings.COLORS.get(0));
  }

  /*
  SETTERS
   */

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void dispose() {
    colors.clear();
  }

  @Override
  public KnowtatorModel getKnowtatorModel() {
    return model;
  }

  /*
  ADDERS
   */

  /**
   * Add color.
   *
   * @param owlClass the owl class
   * @param c the c
   */
  public void addColor(OWLClass owlClass, Color c) {
    colors.put(owlClass, c);
    model.fireColorChanged();
  }

  /**
   * Convert to hex string.
   *
   * @param c the c
   * @return the string
   */
  /*
  TRANSLATORS
   */
  public static String convertToHex(Color c) {
    return String.format("#%06x", c.getRGB() & 0x00FFFFFF);
  }

  public String toString() {
    return id;
  }

  @Override
  public void save() {
    KnowtatorXmlUtil xmlUtil = new KnowtatorXmlUtil();
    xmlUtil.writeFromProfile(this);
  }

  /**
   * Gets save location.
   *
   * @return the save location
   */
  public File getSaveLocation() {
    return new File(model.getSaveLocation().getAbsolutePath(), String.format("%s.xml", id));
  }

  /**
   * Gets colors.
   *
   * @return the colors
   */
  public Map<OWLClass, Color> getColors() {
    return colors;
  }
}
