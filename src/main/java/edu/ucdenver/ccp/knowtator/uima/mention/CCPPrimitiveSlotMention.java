/* Apache UIMA v3 - First created by JCasGen Fri Apr 06 15:53:51 MDT 2018 */

package edu.ucdenver.ccp.knowtator.uima.mention;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/**
 * An abstract class for grouping all primitive (string, float, boolean, integer) slot mentions.
 * Updated by JCasGen Fri Apr 06 16:53:14 MDT 2018 XML source:
 * E:/Documents/GDrive/Projects/Knowtator/KnowtatorStandalone/src/main/resources/CcpTypeSystem.xml
 *
 * @generated
 */
@SuppressWarnings("WeakerAccess")
public class CCPPrimitiveSlotMention extends CCPSlotMention {

  /**
   * The constant _TypeName.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final String _TypeName =
      "edu.ucdenver.ccp.knowtator.uima.mention.CCPPrimitiveSlotMention";

  /**
   * The constant typeIndexID.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(CCPPrimitiveSlotMention.class);
  /**
   * The constant type.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int type = typeIndexID;
  /**
   * Gets type index id.
   *
   * @return index of the type
   * @generated
   */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }

  /* *******************
   *   Feature Offsets *
   * *******************/

  /* Feature Adjusted Offsets */

  /**
   * Never called. Disable default constructor
   *
   * @generated
   */
  protected CCPPrimitiveSlotMention() {
    /* intentionally empty block */
  }

  /**
   * Internal - constructor used by generator
   *
   * @param type the type of this Feature Structure
   * @param casImpl the CAS this Feature Structure belongs to
   * @generated
   */
  public CCPPrimitiveSlotMention(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }

  /**
   * Instantiates a new Ccp primitive slot mention.
   *
   * @param jcas JCas to which this Feature Structure belongs
   * @generated
   */
  public CCPPrimitiveSlotMention(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   *
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable
   */
  private void readObject() {
    /*default - does nothing empty block */
  }
}
