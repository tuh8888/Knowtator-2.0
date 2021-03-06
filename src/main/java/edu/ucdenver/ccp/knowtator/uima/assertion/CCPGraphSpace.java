/* Apache UIMA v3 - First created by JCasGen Fri Apr 06 15:53:51 MDT 2018 */

package edu.ucdenver.ccp.knowtator.uima.assertion;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;

/**
 * Updated by JCasGen Fri Apr 06 16:53:14 MDT 2018 XML source:
 * E:/Documents/GDrive/Projects/Knowtator/KnowtatorStandalone/src/main/resources/CcpTypeSystem.xml
 *
 * @generated
 */
@SuppressWarnings("unchecked")
public class CCPGraphSpace extends TOP {

  /**
   * The constant _TypeName.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final String _TypeName = "edu.ucdenver.ccp.knowtator.uima.assertion.CCPGraphSpace";

  /**
   * The constant typeIndexID.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(CCPGraphSpace.class);
  /**
   * The constant type.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int type = typeIndexID;

  /** The constant _FeatName_graphSpaceID. */
  public static final String _FeatName_graphSpaceID = "graphSpaceID";

  /** The constant _FeatName_vertices. */
  /* *******************
   *   Feature Offsets *
   * *******************/
  public static final String _FeatName_vertices = "vertices";
  /** The constant _FeatName_triples. */
  public static final String _FeatName_triples = "triples";
  /* Feature Adjusted Offsets */
  private static final CallSite _FC_graphSpaceID =
      TypeSystemImpl.createCallSite(CCPGraphSpace.class, "graphSpaceID");
  private static final MethodHandle _FH_graphSpaceID = _FC_graphSpaceID.dynamicInvoker();
  private static final CallSite _FC_vertices =
      TypeSystemImpl.createCallSite(CCPGraphSpace.class, "vertices");
  private static final MethodHandle _FH_vertices = _FC_vertices.dynamicInvoker();
  private static final CallSite _FC_triples =
      TypeSystemImpl.createCallSite(CCPGraphSpace.class, "triples");
  private static final MethodHandle _FH_triples = _FC_triples.dynamicInvoker();

  /**
   * Never called. Disable default constructor
   *
   * @generated
   */
  protected CCPGraphSpace() {
    /* intentionally empty block */
  }

  /**
   * Internal - constructor used by generator
   *
   * @param type the type of this Feature Structure
   * @param casImpl the CAS this Feature Structure belongs to
   * @generated
   */
  public CCPGraphSpace(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }

  /**
   * Instantiates a new Ccp graph space.
   *
   * @param jcas JCas to which this Feature Structure belongs
   * @generated
   */
  public CCPGraphSpace(JCas jcas) {
    super(jcas);
    readObject();
  }

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

  // *--------------*
  // * Feature: graphSpaceID

  /**
   * getter for graphSpaceID - gets
   *
   * @return value of the feature
   * @generated
   */
  public String getGraphSpaceID() {
    return _getStringValueNc(wrapGetIntCatchException(_FH_graphSpaceID));
  }

  /**
   * setter for graphSpaceID - sets
   *
   * @param v value to set into the feature
   * @generated
   */
  public void setGraphSpaceID(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_graphSpaceID), v);
  }

  // *--------------*
  // * Feature: vertices

  /**
   * getter for vertices - gets
   *
   * @return value of the feature
   * @generated
   */
  public FSArray getVertices() {
    return (FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_vertices)));
  }

  /**
   * setter for vertices - sets
   *
   * @param v value to set into the feature
   * @generated
   */
  public void setVertices(FSArray v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_vertices), v);
  }

  /**
   * indexed getter for vertices - gets an indexed value -
   *
   * @param i index in the array to get
   * @return value of the element at index i
   * @generated
   */
  public CCPVertex getVertices(int i) {
    return (CCPVertex)
        (((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_vertices)))).get(i));
  }

  /**
   * indexed setter for vertices - sets an indexed value -
   *
   * @param i index in the array to set
   * @param v value to set into the array
   * @generated
   */
  public void setVertices(int i, CCPVertex v) {
    ((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_vertices)))).set(i, v);
  }

  // *--------------*
  // * Feature: triples

  /**
   * getter for triples - gets
   *
   * @return value of the feature
   * @generated
   */
  public FSArray getTriples() {
    return (FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_triples)));
  }

  /**
   * setter for triples - sets
   *
   * @param v value to set into the feature
   * @generated
   */
  public void setTriples(FSArray v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_triples), v);
  }

  /**
   * indexed getter for triples - gets an indexed value -
   *
   * @param i index in the array to get
   * @return value of the element at index i
   * @generated
   */
  public CCPTriple getTriples(int i) {
    return (CCPTriple)
        (((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_triples)))).get(i));
  }

  /**
   * indexed setter for triples - sets an indexed value -
   *
   * @param i index in the array to set
   * @param v value to set into the array
   * @generated
   */
  public void setTriples(int i, CCPTriple v) {
    ((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_triples)))).set(i, v);
  }
}
