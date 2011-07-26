package com.controlj.green.simplescheds;

import junit.framework.TestCase;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: tirish
 * Date: Jul 25, 2011
 * Time: 9:50:30 AM
 */
public class TestSinglePeriodScheduler extends TestCase
{
   public void testConstruction()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      SinglePeriodScheduler scheduler1 = createTestPeriod(periods, false, false);

      assertEquals(0, scheduler1.getMutablePeriods().size());
      assertNull(scheduler1.getSinglePeriod());
      assertFalse(scheduler1.isMultiplePeriods());
      assertFalse(scheduler1.isHoliday());
      assertFalse(scheduler1.isContinuous());

      SinglePeriodScheduler scheduler2 = createTestPeriod(periods, true, true);

      assertEquals(0, scheduler2.getMutablePeriods().size());
      assertNull(scheduler2.getSinglePeriod());
      assertFalse(scheduler2.isMultiplePeriods());
      assertTrue(scheduler2.isHoliday());
      assertTrue(scheduler2.isContinuous());
   }

   public void testFullDayOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartOfDay().setEndOfDay().setOccupied(true));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNotNull(scheduler.getSinglePeriod());
      assertEquals(periods.first(), scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNull(schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testFullDayUnOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartOfDay().setEndOfDay().setOccupied(false));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNull(schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testFirstHalfDayOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartOfDay().setEndTime(12, 0).setOccupied(true));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNotNull(scheduler.getSinglePeriod());
      assertEquals(periods.first(), scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartTime(12,0).setEndOfDay(), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testFirstHalfDayUnOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartOfDay().setEndTime(12, 0).setOccupied(false));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartTime(12,0).setEndOfDay(), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testLastHalfDayOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartTime(12, 0).setEndOfDay().setOccupied(true));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNotNull(scheduler.getSinglePeriod());
      assertEquals(periods.first(), scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartOfDay().setEndTime(12, 0).setOccupied(true), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testLastHalfDayUnOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartTime(12, 0).setEndOfDay().setOccupied(false));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartOfDay().setEndTime(12, 0).setOccupied(true), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testMiddleOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartTime(8, 0).setEndTime(17, 0).setOccupied(true));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNotNull(scheduler.getSinglePeriod());
      assertEquals(periods.first(), scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartOfDay().setEndTime(8, 0).setOccupied(true), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testMiddleUnOccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartTime(8, 0).setEndTime(17, 0).setOccupied(false));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(1, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartOfDay().setEndTime(8, 0).setOccupied(true), schedulerHoliday.getSinglePeriod());
      assertTrue(schedulerHoliday.isMultiplePeriods());
   }

   public void testMiddleUndefinedSidesUnoccupied()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      periods.add(new MutableSchedulePeriod().setStartOfDay().setEndTime(8, 0).setOccupied(false));
      periods.add(new MutableSchedulePeriod().setStartTime(17, 0).setEndOfDay().setOccupied(false));
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      assertEquals(2, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());
      assertFalse(scheduler.isMultiplePeriods());

      SinglePeriodScheduler schedulerHoliday = createTestPeriod(periods, true, false);

      assertEquals(2, schedulerHoliday.getMutablePeriods().size());
      assertNotNull(schedulerHoliday.getSinglePeriod());
      assertEquals(new MutableSchedulePeriod().setStartTime(8, 0).setEndTime(17, 0).setOccupied(true), schedulerHoliday.getSinglePeriod());
      assertFalse(schedulerHoliday.isMultiplePeriods());
   }

   public void testSetOccupiedPeriod()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);

      MutableSchedulePeriod testPeriod =new MutableSchedulePeriod();
      testPeriod.setOccupied(true);

      //full occupied
      testPeriod.setStartOfDay().setEndOfDay();
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //full unoccupied
      testPeriod.setStartOfDay().setEndTime(0,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(0, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());

      //first half day occupied
      testPeriod.setStartOfDay().setEndTime(12,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //second half day occupied
      testPeriod.setStartTime(12,0).setEndOfDay();
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //middle of day occupied
      testPeriod.setStartTime(8,20).setEndTime(25, 32);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

   }

   public void testSetHolidayPeriod()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();
      SinglePeriodScheduler scheduler = createTestPeriod(periods, true, false);

      MutableSchedulePeriod testPeriod =new MutableSchedulePeriod();
      testPeriod.setOccupied(true);

      //full undefined
      testPeriod.setStartOfDay().setEndOfDay();
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(0, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //full unoccupied
      testPeriod.setStartOfDay().setEndTime(0,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());

      //first half day undefined
      testPeriod.setStartOfDay().setEndTime(12,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //second half day undefined
      testPeriod.setStartTime(12,0).setEndOfDay();
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //middle of day undefined
      testPeriod.setStartTime(8,20).setEndTime(15, 32);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertEquals(2, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //null values but has single period. Nothing should change
      scheduler.setSinglePeriod(null, null);
      assertEquals(2, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //null start, only the end should change
      testPeriod.setEndTime(16,10);
      scheduler.setSinglePeriod(null, testPeriod.getEndTime());
      assertEquals(2, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //null end, only the start should change
      testPeriod.setStartTime(6,10);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), null);
      assertEquals(2, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //remove period
      testPeriod.setStartTime(0,0).setEndTime(0,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertNull(scheduler.getSinglePeriod());

      //null values and has no single period. Nothing should change
      scheduler.setSinglePeriod(null, null);
      assertNull(scheduler.getSinglePeriod());

      //null start, the start should use default, the end should change
      testPeriod.setStartOfDay().setEndTime(16,10);
      scheduler.setSinglePeriod(null, testPeriod.getEndTime());
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //remove period
      testPeriod.setStartTime(0,0).setEndTime(0,0);
      scheduler.setSinglePeriod(testPeriod.getStartTime(), testPeriod.getEndTime());
      assertNull(scheduler.getSinglePeriod());

      //null end, the start should change, the end should use default
      testPeriod.setStartTime(6,10).setEndOfDay();
      scheduler.setSinglePeriod(testPeriod.getStartTime(), null);
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

   }

   public void testSetDefaultPeriod()
   {
      TreeSet<MutableSchedulePeriod> periods = new TreeSet<MutableSchedulePeriod>();

      MutableSchedulePeriod testPeriod =new MutableSchedulePeriod();
      testPeriod.setStartTime(8, 0).setEndTime(17, 0);
      testPeriod.setOccupied(true);

      //normal, not continuous
      SinglePeriodScheduler scheduler = createTestPeriod(periods, false, false);
      scheduler.setDefaultPeriod();
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertEquals(testPeriod, scheduler.getSinglePeriod());

      //holiday, not continuous
      scheduler = createTestPeriod(periods, true, false);
      scheduler.setDefaultPeriod();
      assertEquals(1, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());

      MutableSchedulePeriod testContinuousPeriod =new MutableSchedulePeriod();
      testContinuousPeriod.setStartOfDay().setEndOfDay();

      //normal, continuous
      scheduler = createTestPeriod(periods, false, true);
      scheduler.setDefaultPeriod();
      assertEquals(0, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());

      //holiday, continuous
      scheduler = createTestPeriod(periods, true, true);
      scheduler.setDefaultPeriod();
      assertEquals(0, scheduler.getMutablePeriods().size());
      assertNull(scheduler.getSinglePeriod());

   }

   public SinglePeriodScheduler createTestPeriod(SortedSet<MutableSchedulePeriod> periods, boolean holiday, boolean continuous)
   {
      return new SinglePeriodScheduler(periods, holiday, continuous);
   }
}
