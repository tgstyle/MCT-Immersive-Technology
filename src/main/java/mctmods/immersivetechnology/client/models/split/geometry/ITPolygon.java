package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record ITPolygon<Texture>(List<ITVertex> points, Texture texture) {

    private static final ITEpsilonMath EPS_MATH = ITEpsilonMath.DEFAULT;

    public ITPolygon(List<ITVertex> points, Texture texture) {
        this.points = ImmutableList.copyOf(points);
        this.texture = texture;
    }

    public List<ITVertex> getPoints() {
        return this.points;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public Map<ITEpsilonMath.Sign, ITPolygon<Texture>> splitAlong(ITPlane p) {
        List<ITEpsilonMath.Sign> signs = new ArrayList<>(this.points.size());
        for (ITVertex point : this.points) {
            double product = p.normal().dotProduct(point.position()) - p.dotProduct();
            signs.add(EPS_MATH.sign(product));
        }

        int firstSignStart = 0;
        ITEpsilonMath.Sign zeroSign = signs.get(0);
        for (; firstSignStart < this.points.size(); ++firstSignStart) {
            ITEpsilonMath.Sign signHere = signs.get(firstSignStart);
            if (zeroSign != signHere && signHere != ITEpsilonMath.Sign.ZERO) {
                break;
            }
        }

        if (firstSignStart >= this.points.size()) {
            return Map.of(zeroSign, this);
        } else {
            ITEpsilonMath.Sign firstSign = signs.get(firstSignStart);
            ITEpsilonMath.Sign otherSign = firstSign.invert();
            if (!signs.contains(otherSign)) {
                return Map.of(firstSign, this);
            } else {
                ITCyclicListWrapper<ITEpsilonMath.Sign> cyclicSigns = new ITCyclicListWrapper<>(signs);
                ITCyclicListWrapper<ITVertex> cyclicPoints = new ITCyclicListWrapper<>(this.points);

                int otherSignStart = firstSignStart;
                while (cyclicSigns.get(otherSignStart) != otherSign) {
                    ++otherSignStart;
                }

                List<ITVertex> firstInnerPoints = cyclicPoints.sublist(firstSignStart, otherSignStart);
                List<ITVertex> otherInnerPoints = cyclicPoints.sublist(otherSignStart, firstSignStart);
                ITVertex firstNewPoint = this.intersect(cyclicPoints.get(firstSignStart - 1), cyclicPoints.get(firstSignStart), p);
                ITVertex otherNewPoint = this.intersect(cyclicPoints.get(otherSignStart - 1), cyclicPoints.get(otherSignStart), p);

                List<ITVertex> poly1 = new ArrayList<>();
                poly1.add(firstNewPoint);
                poly1.addAll(firstInnerPoints);
                poly1.add(otherNewPoint);

                List<ITVertex> poly2 = new ArrayList<>();
                poly2.add(otherNewPoint);
                poly2.addAll(otherInnerPoints);
                poly2.add(firstNewPoint);

                return Map.of(
                        firstSign, new ITPolygon<>(poly1, this.getTexture()),
                        otherSign, new ITPolygon<>(poly2, this.getTexture())
                );
            }
        }
    }

    private ITVertex intersect(ITVertex a, ITVertex b, ITPlane p) {
        double productA = a.position().dotProduct(p.normal());
        double productB = b.position().dotProduct(p.normal());
        double lambda = (p.dotProduct() - productB) / (productA - productB);
        return ITVertex.interpolate(a, b, lambda);
    }

    public ITPolygon<Texture> translate(int axis, double amount) {
        List<ITVertex> translatedVertices = new ArrayList<>(this.points.size());
        for (ITVertex v : this.points) {
            translatedVertices.add(v.translate(axis, amount));
        }
        return new ITPolygon<>(translatedVertices, this.texture);
    }

    public ITPolygon<Texture> translate(ITVec3d offset) {
        List<ITVertex> translatedVertices = new ArrayList<>(this.points.size());
        for (ITVertex v : this.points) {
            translatedVertices.add(v.translate(offset));
        }
        return new ITPolygon<>(translatedVertices, this.texture);
    }

    public List<ITPolygon<Texture>> quadify() {
        List<ITPolygon<Texture>> quads = new ArrayList<>();
        int secondVertex;
        for (secondVertex = 1; secondVertex + 2 < this.points.size(); secondVertex += 2) {
            quads.add(new ITPolygon<>(List.of(
                    this.points.get(0),
                    this.points.get(secondVertex),
                    this.points.get(secondVertex + 1),
                    this.points.get(secondVertex + 2)
            ), this.getTexture()));
        }
        if (secondVertex + 1 < this.points.size()) {
            quads.add(new ITPolygon<>(List.of(
                    this.points.get(0),
                    this.points.get(secondVertex),
                    this.points.get(secondVertex + 1),
                    this.points.get(secondVertex + 1)
            ), this.getTexture()));
        }
        return quads;
    }
}
