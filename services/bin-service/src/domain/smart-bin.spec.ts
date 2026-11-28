import { SmartBin } from './smart-bin';

describe('SmartBin.applyReading (FR3.2 flag bins at threshold)', () => {
  const makeBin = (threshold = 80) =>
    new SmartBin('bin-1', 'user-1', 'device-1', 'Cairo', threshold);

  it('flags NEEDS_COLLECTION when fill level equals the threshold', () => {
    const bin = makeBin(80);
    bin.applyReading(80, new Date());
    expect(bin.status).toBe('NEEDS_COLLECTION');
  });

  it('flags NEEDS_COLLECTION when fill level is above the threshold', () => {
    const bin = makeBin(80);
    bin.applyReading(95, new Date());
    expect(bin.status).toBe('NEEDS_COLLECTION');
  });

  it('stays NORMAL below the threshold', () => {
    const bin = makeBin(80);
    bin.applyReading(79, new Date());
    expect(bin.status).toBe('NORMAL');
  });

  it('records the time of the last reading', () => {
    const bin = makeBin();
    const at = new Date('2026-10-28T20:00:00Z');
    bin.applyReading(10, at);
    expect(bin.lastReadingAt).toEqual(at);
  });

  it('rejects readings outside 0-100', () => {
    const bin = makeBin();
    expect(() => bin.applyReading(101, new Date())).toThrow(RangeError);
    expect(() => bin.applyReading(-1, new Date())).toThrow(RangeError);
  });
});
