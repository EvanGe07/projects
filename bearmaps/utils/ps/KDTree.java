package bearmaps.utils.ps;

import java.util.List;

public class KDTree implements PointSet {

    private KDTreeNode root;

    public KDTree() {
        root = null;
    }

    /* Constructs a KDTree using POINTS. You can assume POINTS contains at least one
       Point object. */
    public KDTree(List<? extends Point> points) {
        this();
        for (Point t : points) {
            insert(t);
        }
    }

    /*

    You might find this insert helper method useful when constructing your KDTree!
    Think of what arguments you might want insert to take in. If you need
    inspiration, take a look at how we do BST insertion!

    private KDTreeNode insert(...) {
        ...
    }

    */

    /* Returns the closest Point to the inputted X and Y coordinates. This method
       should run in O(log N) time on average, where N is the number of POINTS. */
    @Override
    public Point nearest(double x, double y) {
        return nearest(root, root.point(), new Point(x, y));
    }

    public <E extends Point> Point nearest(KDTreeNode curr, Point currBest, Point toFind) {
        if (curr == null) {
            return currBest;
        }
        Point currPoint = curr.point();
        Point toReturn = currBest;
        if (Point.distance(currPoint, toFind) < Point.distance(currBest, toFind)) {
            toReturn = currPoint;
        }
        if (curr.isVertical) { // Vertical split. Look for Y.
            if (toFind.getY() < currPoint.getY()) { // Smaller. Look left.
                Point tmp = nearest(curr.left, toReturn, toFind);
                if (Point.distance(tmp, toFind)
                    > Point.distance(new Point(toFind.getX(), currPoint.getY()), toFind)) {
                    tmp = nearest(curr.right, tmp, toFind);
                }
                if (Point.distance(tmp, toFind) < Point.distance(toReturn, toFind)) {
                    toReturn = tmp;
                }
            } else {    // Larger. Look right.
                Point tmp = nearest(curr.right, toReturn, toFind);
                if (Point.distance(tmp, toFind)
                    > Point.distance(new Point(toFind.getX(), currPoint.getY()), toFind)) {
                    tmp = nearest(curr.left, tmp, toFind);
                }
                if (Point.distance(tmp, toFind) < Point.distance(toReturn, toFind)) {
                    toReturn = tmp;
                }
            }
        } else { // Horizontal split. Look for X.
            if (toFind.getX() < currPoint.getX()) { // Smaller. Look left.
                Point tmp = nearest(curr.left, toReturn, toFind);
                if (Point.distance(tmp, toFind)
                    > Point.distance(new Point(currPoint.getX(), toFind.getY()), toFind)) {
                    tmp = nearest(curr.right, tmp, toFind);
                }
                if (Point.distance(tmp, toFind) < Point.distance(toReturn, toFind)) {
                    toReturn = tmp;
                }
            } else {    // Bigger. Look right.
                Point tmp = nearest(curr.right, toReturn, toFind);
                if (Point.distance(tmp, toFind)
                    > Point.distance(new Point(currPoint.getX(), toFind.getY()), toFind)) {
                    tmp = nearest(curr.left, tmp, toFind);
                }
                if (Point.distance(tmp, toFind) < Point.distance(toReturn, toFind)) {
                    toReturn = tmp;
                }
            }
        }
        return toReturn;
    }

    public void insert(Point data) {
        if (root == null) {
            root = new KDTreeNode(data, false);
        } else {
            insert(root, data);
        }
    }

    private void insert(KDTreeNode curr, Point data) {
        if (curr.isVertical) {
            if (data.getY() < curr.point().getY()) {
                if (curr.left == null) {
                    curr.left = new KDTreeNode(data, false);
                } else {
                    insert(curr.left, data);
                }
            } else {
                if (curr.right == null) {
                    curr.right = new KDTreeNode(data, false);
                } else {
                    insert(curr.right, data);
                }
            }
        } else {
            if (data.getX() < curr.point().getX()) {
                if (curr.left == null) {
                    curr.left = new KDTreeNode(data, true);
                } else {
                    insert(curr.left, data);
                }
            } else {
                if (curr.right == null) {
                    curr.right = new KDTreeNode(data, true);
                } else {
                    insert(curr.right, data);
                }
            }
        }
    }

    private class KDTreeNode {

        private Point point;
        private KDTreeNode left;
        private KDTreeNode right;

        private boolean isVertical;

        // If you want to add any more instance variables, put them here!

        KDTreeNode(Point p) {
            this.point = p;
        }

        KDTreeNode(Point p, KDTreeNode left, KDTreeNode right) {
            this.point = p;
            this.left = left;
            this.right = right;
        }

        KDTreeNode(Point p, KDTreeNode left, KDTreeNode right, boolean isVertical) {
            this(p, left, right);
            this.isVertical = isVertical;
        }

        KDTreeNode(Point p, boolean isVertical) {
            this(p);
            this.isVertical = isVertical;
        }

        Point point() {
            return point;
        }

        KDTreeNode left() {
            return left;
        }

        KDTreeNode right() {
            return right;
        }

        // If you want to add any more methods, put them here!

    }
}
