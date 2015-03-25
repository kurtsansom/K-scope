//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2015.01.08 at 01:05:45 AM JST
//


package jp.riken.kscope.xcodeml.clang.xml.gen;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jp.riken.kscope.xcodeml.clang.xml.*;



/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;group ref="{}expressions"/>
 *         &lt;element name="value" type="{}compoundValue"/>
 *         &lt;element ref="{}designatedValue"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{}annotation"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "designatedValue",
    "expressionOrValue"
})
@XmlRootElement(name = "value")
public class Value
    implements  IXmlNode
{

    protected DesignatedValue designatedValue;
    @XmlAttribute(name = "is_gccSyntax")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isGccSyntax;
    @XmlAttribute(name = "is_modified")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isModified;

    @XmlElements({
        @XmlElement(name = "intConstant", type = IntConstant.class),
        @XmlElement(name = "floatConstant", type = FloatConstant.class),
        @XmlElement(name = "longlongConstant", type = LonglongConstant.class),
        @XmlElement(name = "stringConstant", type = StringConstant.class),
        @XmlElement(name = "moeConstant", type = MoeConstant.class),
        @XmlElement(name = "funcAddr", type = FuncAddr.class),
        @XmlElement(name = "pointerRef", type = PointerRef.class),
        @XmlElement(name = "Var", type = Var.class),
        @XmlElement(name = "varAddr", type = VarAddr.class),
        @XmlElement(name = "arrayRef", type = ArrayRef.class),
        @XmlElement(name = "arrayAddr", type = ArrayAddr.class),
        @XmlElement(name = "memberAddr", type = MemberAddr.class),
        @XmlElement(name = "memberRef", type = MemberRef.class),
        @XmlElement(name = "memberArrayRef", type = MemberArrayRef.class),
        @XmlElement(name = "memberArrayAddr", type = MemberArrayAddr.class),
        @XmlElement(name = "assignExpr", type = AssignExpr.class),
        @XmlElement(name = "plusExpr", type = PlusExpr.class),
        @XmlElement(name = "minusExpr", type = MinusExpr.class),
        @XmlElement(name = "mulExpr", type = MulExpr.class),
        @XmlElement(name = "divExpr", type = DivExpr.class),
        @XmlElement(name = "modExpr", type = ModExpr.class),
        @XmlElement(name = "LshiftExpr", type = LshiftExpr.class),
        @XmlElement(name = "RshiftExpr", type = RshiftExpr.class),
        @XmlElement(name = "bitAndExpr", type = BitAndExpr.class),
        @XmlElement(name = "bitOrExpr", type = BitOrExpr.class),
        @XmlElement(name = "bitXorExpr", type = BitXorExpr.class),
        @XmlElement(name = "asgPlusExpr", type = AsgPlusExpr.class),
        @XmlElement(name = "asgMinusExpr", type = AsgMinusExpr.class),
        @XmlElement(name = "asgMulExpr", type = AsgMulExpr.class),
        @XmlElement(name = "asgDivExpr", type = AsgDivExpr.class),
        @XmlElement(name = "asgModExpr", type = AsgModExpr.class),
        @XmlElement(name = "asgLshiftExpr", type = AsgLshiftExpr.class),
        @XmlElement(name = "asgRshiftExpr", type = AsgRshiftExpr.class),
        @XmlElement(name = "asgBitAndExpr", type = AsgBitAndExpr.class),
        @XmlElement(name = "asgBitOrExpr", type = AsgBitOrExpr.class),
        @XmlElement(name = "asgBitXorExpr", type = AsgBitXorExpr.class),
        @XmlElement(name = "logEQExpr", type = LogEQExpr.class),
        @XmlElement(name = "logNEQExpr", type = LogNEQExpr.class),
        @XmlElement(name = "logGEExpr", type = LogGEExpr.class),
        @XmlElement(name = "logGTExpr", type = LogGTExpr.class),
        @XmlElement(name = "logLEExpr", type = LogLEExpr.class),
        @XmlElement(name = "logLTExpr", type = LogLTExpr.class),
        @XmlElement(name = "logAndExpr", type = LogAndExpr.class),
        @XmlElement(name = "logOrExpr", type = LogOrExpr.class),
        @XmlElement(name = "unaryMinusExpr", type = UnaryMinusExpr.class),
        @XmlElement(name = "bitNotExpr", type = BitNotExpr.class),
        @XmlElement(name = "logNotExpr", type = LogNotExpr.class),
        @XmlElement(name = "functionCall", type = FunctionCall.class),
        @XmlElement(name = "commaExpr", type = CommaExpr.class),
        @XmlElement(name = "postIncrExpr", type = PostIncrExpr.class),
        @XmlElement(name = "postDecrExpr", type = PostDecrExpr.class),
        @XmlElement(name = "preIncrExpr", type = PreIncrExpr.class),
        @XmlElement(name = "preDecrExpr", type = PreDecrExpr.class),
        @XmlElement(name = "castExpr", type = CastExpr.class),
        @XmlElement(name = "condExpr", type = CondExpr.class),
        @XmlElement(name = "sizeOfExpr", type = SizeOfExpr.class),
        @XmlElement(name = "addrOfExpr", type = AddrOfExpr.class),
        @XmlElement(name = "xmpDescOf", type = XmpDescOf.class),
        @XmlElement(name = "compoundValue", type = CompoundValueExpr.class),
        @XmlElement(name = "compoundValueAddr", type = CompoundValueAddr.class),
        @XmlElement(name = "gccAlignOfExpr", type = GccAlignOfExpr.class),
        @XmlElement(name = "gccLabelAddr", type = GccLabelAddr.class),
        @XmlElement(name = "gccCompoundExpr", type = GccCompoundExpr.class),
        @XmlElement(name = "builtin_op", type = BuiltinOp.class),
        @XmlElement(name = "subArrayRef", type = SubArrayRef.class),
        @XmlElement(name = "coArrayRef", type = CoArrayRef.class),
        @XmlElement(name = "coArrayAssignExpr", type = CoArrayAssignExpr.class),
        @XmlElement(name = "value", type = Value.class)
    })
    protected List<IXmlNode> expressionOrValue;

    /**
     * Gets the value of the designatedValue property.
     *
     * @return
     *     possible object is
     *     {@link DesignatedValue }
     *
     */
    public DesignatedValue getDesignatedValue() {
        return designatedValue;
    }

    /**
     * Sets the value of the designatedValue property.
     *
     * @param value
     *     allowed object is
     *     {@link DesignatedValue }
     *
     */
    public void setDesignatedValue(DesignatedValue value) {
        this.designatedValue = value;
    }

    /**
     * Gets the value of the isGccSyntax property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIsGccSyntax() {
        return isGccSyntax;
    }

    /**
     * Sets the value of the isGccSyntax property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIsGccSyntax(String value) {
        this.isGccSyntax = value;
    }

    /**
     * Gets the value of the isModified property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIsModified() {
        return isModified;
    }

    /**
     * Sets the value of the isModified property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIsModified(String value) {
        this.isModified = value;
    }


    /**
     * Gets the value of the expressions property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the expressions property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExpressions().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IntConstant }
     * {@link FloatConstant }
     * {@link LonglongConstant }
     * {@link StringConstant }
     * {@link MoeConstant }
     * {@link FuncAddr }
     * {@link PointerRef }
     * {@link Var }
     * {@link VarAddr }
     * {@link ArrayRef }
     * {@link ArrayAddr }
     * {@link MemberAddr }
     * {@link MemberRef }
     * {@link MemberArrayRef }
     * {@link MemberArrayAddr }
     * {@link AssignExpr }
     * {@link PlusExpr }
     * {@link MinusExpr }
     * {@link MulExpr }
     * {@link DivExpr }
     * {@link ModExpr }
     * {@link LshiftExpr }
     * {@link RshiftExpr }
     * {@link BitAndExpr }
     * {@link BitOrExpr }
     * {@link BitXorExpr }
     * {@link AsgPlusExpr }
     * {@link AsgMinusExpr }
     * {@link AsgMulExpr }
     * {@link AsgDivExpr }
     * {@link AsgModExpr }
     * {@link AsgLshiftExpr }
     * {@link AsgRshiftExpr }
     * {@link AsgBitAndExpr }
     * {@link AsgBitOrExpr }
     * {@link AsgBitXorExpr }
     * {@link LogEQExpr }
     * {@link LogNEQExpr }
     * {@link LogGEExpr }
     * {@link LogGTExpr }
     * {@link LogLEExpr }
     * {@link LogLTExpr }
     * {@link LogAndExpr }
     * {@link LogOrExpr }
     * {@link UnaryMinusExpr }
     * {@link BitNotExpr }
     * {@link LogNotExpr }
     * {@link FunctionCall }
     * {@link CommaExpr }
     * {@link PostIncrExpr }
     * {@link PostDecrExpr }
     * {@link PreIncrExpr }
     * {@link PreDecrExpr }
     * {@link CastExpr }
     * {@link CondExpr }
     * {@link SizeOfExpr }
     * {@link AddrOfExpr }
     * {@link XmpDescOf }
     * {@link CompoundValueExpr }
     * {@link CompoundValueAddr }
     * {@link GccAlignOfExpr }
     * {@link GccLabelAddr }
     * {@link GccCompoundExpr }
     * {@link BuiltinOp }
     * {@link SubArrayRef }
     * {@link CoArrayRef }
     * {@link CoArrayAssignExpr }
     *
     *
     */
    public List<IXmlNode> getExpressionsOrValues() {
        if (expressionOrValue == null) {
            expressionOrValue = new ArrayList<IXmlNode>();
        }
        return this.expressionOrValue;
    }



    @Override
    public boolean enter(jp.riken.kscope.xcodeml.clang.xml.IXmlVisitor visitor) {
        return (visitor.enter(this));
    }

    @Override
    public void leave(jp.riken.kscope.xcodeml.clang.xml.IXmlVisitor visitor) {
        visitor.leave(this);
    }


}
